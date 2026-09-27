package com.example.silentanticheat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central place for sending anti-cheat alerts.
 * Alerts NEVER take punitive action -- console log + op-only chat message, nothing else.
 *
 * Repeated alerts for the same player/check are coalesced into a single message.
 * For example, a burst of four speed detections is reported as "(4x)" instead
 * of producing four separate operator chat messages.
 */
public final class AlertUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger("SilentAntiCheat");

    private static final Style ALERT_STYLE = Style.EMPTY
            .withColor(ChatFormatting.DARK_RED)
            .withBold(true);

    private static final long ALERT_AGGREGATION_WINDOW_NANOS = 5_000_000_000L;
    private static final long ALERT_STATE_RETENTION_NANOS = 60_000_000_000L;

    private static final Map<AlertKey, AlertState> ALERT_STATES = new HashMap<>();

    private AlertUtil() {
    }

    /**
     * Logs to console and pings every online op with a styled chat message.
     *
     * Repeated alerts for the same suspect/check within the aggregation window
     * are counted rather than emitted individually.
     */
    public static synchronized void alert(
            MinecraftServer server,
            ServerPlayer suspect,
            String checkName,
            String detail
    ) {
        String suspectName = suspect.getName().getString();
        long now = System.nanoTime();

        // Keep the aggregation map bounded if old player/check combinations remain.
        ALERT_STATES.entrySet().removeIf(entry ->
                now - entry.getValue().lastEmissionNanos > ALERT_STATE_RETENTION_NANOS);

        AlertKey key = new AlertKey(suspect.getUUID(), checkName);
        AlertState state = ALERT_STATES.get(key);

        if (state != null && now - state.lastEmissionNanos < ALERT_AGGREGATION_WINDOW_NANOS) {
            state.suppressedCount++;
            return;
        }

        int totalCount = state == null ? 1 : state.suppressedCount + 1;
        ALERT_STATES.put(key, new AlertState(now));

        String countSuffix = totalCount > 1 ? " (" + totalCount + "x)" : "";

        String consoleMessage = "[SilentAntiCheat] " + suspectName
                + " flagged " + checkName + countSuffix + " - " + detail;

        LOGGER.warn(consoleMessage);

        Component opMessage = Component.literal("[AntiCheat] " + suspectName
                        + " failed " + checkName + " check" + countSuffix + ": " + detail)
                .setStyle(ALERT_STYLE);

        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            // Permission level 2 == /op default. Use the server's operator list directly.
            if (server.getPlayerList().isOp(online.nameAndId())) {
                online.sendSystemMessage(opMessage);
            }
        }
    }

    private record AlertKey(UUID playerId, String checkName) {
    }

    private static final class AlertState {
        private final long lastEmissionNanos;
        private int suppressedCount;

        private AlertState(long lastEmissionNanos) {
            this.lastEmissionNanos = lastEmissionNanos;
        }
    }
}
