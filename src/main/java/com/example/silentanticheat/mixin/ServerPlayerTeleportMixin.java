package com.example.silentanticheat.mixin;

import com.example.silentanticheat.checks.MovementCheckListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerTeleportMixin {

    @Inject(method = "teleportTo", at = @At("TAIL"))
    private void silentAnticheat$markServerTeleport(
            ServerLevel level,
            double x,
            double y,
            double z,
            Set<?> relatives,
            float newYRot,
            float newXRot,
            boolean resetCamera,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (Boolean.TRUE.equals(callback.getReturnValue())) {
            MovementCheckListener.markServerTeleport((ServerPlayer) (Object) this);
        }
    }
}
