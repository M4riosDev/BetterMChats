package com.bettermchats.bettermchats;

import com.bettermchats.bettermchats.config.ServerHudConfig;
import com.bettermchats.bettermchats.network.BetterMChatsNetworking;
import com.bettermchats.bettermchats.network.HudConfigPacket;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ServerHudSync {

    private static final boolean ADMIN_UPDATE_NOTICE_ENABLED = true;

    private static final String GITHUB_API_URL = "https://api.github.com/repos/M4riosDev/BetterMChats/releases/latest";
    private static final int HTTP_TIMEOUT_MS = 5000;

    private static final Map<UUID, String> LAST_NOTIFIED_VERSION = new HashMap<>();

    private static volatile boolean UPDATE_CHECK_DONE = false;
    private static volatile boolean UPDATE_AVAILABLE = false;
    private static volatile String CURRENT_VERSION = "unknown";
    private static volatile String RESOLVED_LATEST_VERSION = "";
    private static volatile String RESOLVED_DOWNLOAD_URL = "";

    public static void onPlayerJoin(ServerPlayerEntity sp) {
        if (sp == null) return;

        HudConfigPacket pkt = new HudConfigPacket(
                ServerHudConfig.anchorToInt(ServerHudConfig.anchor),
                ServerHudConfig.offsetX,
                ServerHudConfig.offsetY,
                ServerHudConfig.width,
                ServerHudConfig.lineHeight,
                ServerHudConfig.gap,
                ServerHudConfig.maxEntries,
                ServerHudConfig.lifeMs,
                ServerHudConfig.fadeMs,
                ServerHudConfig.showIcon,
                ServerHudConfig.iconSize
        );

        BetterMChatsNetworking.sendHudConfig(sp, pkt);
        sendAdminUpdateNotice(sp);
    }

    private static void sendAdminUpdateNotice(ServerPlayerEntity player) {
        if (!player.hasPermissionLevel(3)) return;
        if (!ADMIN_UPDATE_NOTICE_ENABLED) return;

        if (!UPDATE_CHECK_DONE) {
            CompletableFuture.runAsync(ServerHudSync::runUpdateCheck);
            return;
        }

        if (!UPDATE_AVAILABLE) return;

        String currentVersion = CURRENT_VERSION;
        String latestVersion = RESOLVED_LATEST_VERSION;
        String downloadUrl = RESOLVED_DOWNLOAD_URL;

        UUID playerId = player.getUuid();
        String lastNotified = LAST_NOTIFIED_VERSION.get(playerId);
        if (latestVersion.equalsIgnoreCase(lastNotified)) return;

        downloadUrl = downloadUrl == null ? "" : downloadUrl.trim();

        String line1 = "[emoji=system][label=SYSTEM][box][color=#FFF200] New update is ready to install.";
        String line2 = "[emoji=system][label=SYSTEM][box][color=#FFF200] Current: " + currentVersion
                + " | Latest: " + latestVersion
                + (downloadUrl.isEmpty() ? "" : " | Download: " + downloadUrl);

        BetterMChatsNetworking.sendChannelMsg(player, line1);
        BetterMChatsNetworking.sendChannelMsg(player, line2);
        LAST_NOTIFIED_VERSION.put(playerId, latestVersion);
    }

    private static void runUpdateCheck() {
        String currentVersion = FabricLoader.getInstance()
                .getModContainer(BetterMChats.MODID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");

        UpdateInfo info = fetchLatestVersion();

        CURRENT_VERSION = currentVersion;
        RESOLVED_LATEST_VERSION = info.latestVersion;
        RESOLVED_DOWNLOAD_URL = info.downloadUrl;
        UPDATE_AVAILABLE = !info.latestVersion.isEmpty() && isNewerVersion(info.latestVersion, currentVersion);
        UPDATE_CHECK_DONE = true;
    }

    private static UpdateInfo fetchLatestVersion() {
        try {
            String json = httpGet(GITHUB_API_URL);
            if (!json.isEmpty()) {
                String tag = extractJsonString(json, "tag_name");
                String htmlUrl = extractJsonString(json, "html_url");
                String version = extractVersionToken(tag);
                if (!version.isEmpty()) {
                    BetterMChats.LOGGER.info("[BetterMChats] Update check OK (GitHub): {}", version);
                    return new UpdateInfo(version, htmlUrl);
                }
            }
        } catch (Exception ex) {
            BetterMChats.LOGGER.warn("[BetterMChats] Update check failed: {}", ex.getMessage());
        }
        return UpdateInfo.empty();
    }

    private static String extractJsonString(String json, String key) {
        if (json == null || key == null) return "";
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
        return "";
    }

    private static String extractVersionToken(String text) {
        if (text == null) return "";
        Matcher m = Pattern.compile("(\\d+\\.\\d+(?:\\.\\d+)*)").matcher(text);
        if (m.find()) return m.group(1);
        return "";
    }

    private static String httpGet(String urlText) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlText).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(HTTP_TIMEOUT_MS);
        conn.setReadTimeout(HTTP_TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "BetterMChats-UpdateChecker/1.0");
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        if (code != 200) throw new Exception("HTTP " + code + " from " + urlText);

        try (InputStream in = conn.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
            return out.toString("UTF-8");
        }
    }

    private static final class UpdateInfo {
        final String latestVersion;
        final String downloadUrl;

        UpdateInfo(String latestVersion, String downloadUrl) {
            this.latestVersion = latestVersion == null ? "" : latestVersion.trim();
            this.downloadUrl = downloadUrl == null ? "" : downloadUrl.trim();
        }

        static UpdateInfo empty() {
            return new UpdateInfo("", "");
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        if (latest == null || current == null) return false;

        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");
        int max = Math.max(latestParts.length, currentParts.length);

        for (int i = 0; i < max; i++) {
            int latestNum = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
            int currentNum = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;

            if (latestNum > currentNum) return true;
            if (latestNum < currentNum) return false;
        }

        return false;
    }

    private static int parseVersionPart(String part) {
        if (part == null) return 0;
        String digits = part.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
