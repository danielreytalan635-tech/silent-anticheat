package com.example.silentanticheat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central place for sending anti-cheat alerts.
 * Alerts NEVER take punitive action -- console log + op-only chat message, nothing else.
 */
public final class AlertUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger("SilentAntiCheat");

    private static final Style ALERT_STYLE = Style.EMPTY
            .withColor(ChatFormatting.DARK_RED)
            .withBold(true);

    private AlertUtil() {
    }

    /**
     * Logs to console and pings every online op with a styled chat message.
     */
    public static void alert(MinecraftServer server, ServerPlayer suspect, String checkName, String detail) {
        String suspectName = suspect.getName().getString();

        String consoleMessage = "[SilentAntiCheat] " + suspectName
                + " flagged " + checkName + " - " + detail;

        LOGGER.warn(consoleMessage);

        Component opMessage = Component.literal("[AntiCheat] " + suspectName
                        + " failed " + checkName + " check: " + detail)
                .setStyle(ALERT_STYLE);

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            // Permission level 2 == /op default. Use the server's operator list directly.
            if (server.getPlayerList().isOp(online.getGameProfile())) {
                online.sendSystemMessage(opMessage);
            }
        }
    }
}
