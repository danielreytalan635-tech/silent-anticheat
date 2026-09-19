package com.example.silentanticheat.checks;

import com.example.silentanticheat.AlertUtil;
import com.example.silentanticheat.PlayerCheckData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-tick player movement for two checks:
 *  - Flight/Hover: too many consecutive airborne ticks with no legitimate reason.
 *  - Speed/Teleport: moved further in one tick than should be legally possible.
 *
 * These thresholds are deliberately conservative to avoid false positives; tune
 * them to your server's allowed potion effects / mods before relying on them.
 */
public final class MovementCheckListener {

    private static final Map<UUID, PlayerCheckData> DATA = new HashMap<>();

    // Consecutive ticks of unexplained airtime before we alert (100 ticks = 5s).
    private static final int MAX_AIRBORNE_TICKS = 100;

    // Blatant single-tick displacement that is never legitimate (teleport hacks,
    // extreme speed hacks). Real vanilla movement, even sprint-jumping with a
    // Speed II potion, stays well under this.
    private static final double MAX_DISTANCE_PER_TICK = 10.0;

    private MovementCheckListener() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(MovementCheckListener::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerCheckData data = DATA.computeIfAbsent(player.getUUID(), id -> new PlayerCheckData(player.position()));

            Vec3 currentPos = player.position();
            double distanceMoved = currentPos.distanceTo(data.lastPosition);

            checkSpeedAndTeleport(server, player, distanceMoved);
            checkFlight(server, player, data);

            data.lastPosition = currentPos;
        }

        // Clean up disconnected players so the map doesn't grow forever.
        DATA.keySet().removeIf(uuid -> server.getPlayerList().getPlayer(uuid) == null);
    }

    private static void checkSpeedAndTeleport(MinecraftServer server, ServerPlayer player, double distanceMoved) {
        if (player.isFallFlying() || player.getVehicle() != null || player.isSpectator()) {
            return; // Elytra, minecarts/boats, and spectators legitimately cover huge distances.
        }

        if (distanceMoved > MAX_DISTANCE_PER_TICK) {
            AlertUtil.alert(server, player, "Speed/Teleport",
                    String.format("moved %.2f blocks in a single tick", distanceMoved));
        }
    }

    private static void checkFlight(MinecraftServer server, ServerPlayer player, PlayerCheckData data) {
        if (isExemptFromFlightCheck(player)) {
            data.airborneTicks = 0;
            data.flightAlertSent = false;
            return;
        }

        if (player.onGround()) {
            data.airborneTicks = 0;
            data.flightAlertSent = false;
            return;
        }

        data.airborneTicks++;

        if (data.airborneTicks >= MAX_AIRBORNE_TICKS && !data.flightAlertSent) {
            AlertUtil.alert(server, player, "Flight/Hover",
                    "airborne for " + data.airborneTicks + " consecutive ticks with no legitimate cause");
            data.flightAlertSent = true; // avoid re-alerting every tick while still airborne
        }
    }

    private static boolean isExemptFromFlightCheck(ServerPlayer player) {
        if (player.getAbilities().flying) return true;          // Creative/Spectator fly
        if (player.isFallFlying()) return true;                  // Elytra
        if (player.onClimbable()) return true;                   // Ladders, vines
        if (player.isInWater() || player.isInLava()) return true;
        if (player.isSleeping()) return true;
        if (player.getVehicle() != null) return true;
        if (player.isSpectator()) return true;

        BlockPos feetPos = player.blockPosition();
        BlockState feetState = player.level().getBlockState(feetPos);
        BlockState belowState = player.level().getBlockState(feetPos.below());

        if (feetState.is(Blocks.COBWEB) || belowState.is(Blocks.COBWEB)) return true;
        if (feetState.is(Blocks.SCAFFOLDING) || belowState.is(Blocks.SCAFFOLDING)) return true;

        return false;
    }
}
