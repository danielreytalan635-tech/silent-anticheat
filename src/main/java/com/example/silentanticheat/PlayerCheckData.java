package com.example.silentanticheat;

import net.minecraft.world.phys.Vec3;

/**
 * Per-player rolling state used by the checks between ticks.
 */
public class PlayerCheckData {

    public Vec3 lastPosition;
    public int airborneTicks = 0;
    public boolean flightAlertSent = false;

    public PlayerCheckData(Vec3 initialPosition) {
        this.lastPosition = initialPosition;
    }
}
