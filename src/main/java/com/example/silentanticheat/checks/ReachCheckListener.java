package com.example.silentanticheat.checks;

import com.example.silentanticheat.AlertUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

/**
 * Flags attacks or block breaks performed from further than 5 blocks away.
 * This is a detection-only listener -- it never cancels the event.
 */
public final class ReachCheckListener {

    private static final double MAX_REACH_DISTANCE = 5.0;

    private ReachCheckListener() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                Vec3 eyePos = serverPlayer.getEyePosition();
                double distance = eyePos.distanceTo(entity.position());

                if (distance > MAX_REACH_DISTANCE) {
                    AlertUtil.alert(serverPlayer.getServer(), serverPlayer, "Reach (Attack)",
                            String.format("attacked an entity from %.2f blocks away", distance));
                }
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                Vec3 eyePos = serverPlayer.getEyePosition();
                Vec3 blockCenter = pos.getCenter();
                double distance = eyePos.distanceTo(blockCenter);

                if (distance > MAX_REACH_DISTANCE) {
                    AlertUtil.alert(serverPlayer.getServer(), serverPlayer, "Reach (Block Break)",
                            String.format("broke a block from %.2f blocks away", distance));
                }
            }
            return true; // Never cancel -- detection only.
        });
    }
}
