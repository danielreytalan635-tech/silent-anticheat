package com.example.silentanticheat;

import com.example.silentanticheat.checks.MovementCheckListener;
import com.example.silentanticheat.checks.ReachCheckListener;
import net.fabricmc.api.DedicatedServerModInitializer;

public class SilentAntiCheat implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        AlertUtil.LOGGER.info("[SilentAntiCheat] Initializing -- silent, alert-only detection active.");

        MovementCheckListener.register();
        ReachCheckListener.register();

        AlertUtil.LOGGER.info("[SilentAntiCheat] Flight, speed/teleport, and reach checks registered.");
    }
}
