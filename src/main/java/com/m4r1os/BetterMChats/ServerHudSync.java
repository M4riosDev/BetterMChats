package com.m4r1os.BetterMChats;

import com.m4r1os.BetterMChats.config.ServerHudConfig;
import com.m4r1os.BetterMChats.network.ChannelMsgPacket;
import com.m4r1os.BetterMChats.network.HudConfigPacket;
import com.m4r1os.BetterMChats.network.ModNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = FiveMHudMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerHudSync {

    private static final boolean ADMIN_UPDATE_NOTICE_ENABLED = true;
    private static final String CURSEFORGE_FILES_URL = "https://www.curseforge.com/minecraft/mc-mods/roleplay-chats/files/all?page=1&pageSize=20&showAlphaFiles=hide";
    private static final String CURSEFORGE_FILE_URL_PREFIX = "https://www.curseforge.com/minecraft/mc-mods/roleplay-chats/files/";
    private static final int HTTP_TIMEOUT_MS = 5000;

    private static final Map<UUID, String> LAST_NOTIFIED_VERSION = new HashMap<>();

    private static volatile boolean UPDATE_CHECK_DONE = false;
    private static volatile boolean UPDATE_AVAILABLE = false;
    private static volatile String CURRENT_VERSION = "unknown";
    private static volatile String RESOLVED_LATEST_VERSION = "";
    private static volatile String RESOLVED_DOWNLOAD_URL = "";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        Player player = e.getEntity();
        if (player == null || player.getLevel().isClientSide()) return;
        if (!(player instanceof ServerPlayer)) return;

        ServerPlayer sp = (ServerPlayer) player;

        HudConfigPacket pkt = new HudConfigPacket(
                ServerHudConfig.anchorToInt(ServerHudConfig.ANCHOR.get()),
                ServerHudConfig.OFFSET_X.get(),
                ServerHudConfig.OFFSET_Y.get(),
                ServerHudConfig.WIDTH.get(),
                ServerHudConfig.LINE_HEIGHT.get(),
                ServerHudConfig.GAP.get(),
                ServerHudConfig.MAX_ENTRIES.get(),
                ServerHudConfig.LIFE_MS.get(),
                ServerHudConfig.FADE_MS.get(),
                ServerHudConfig.SHOW_ICON.get(),
                ServerHudConfig.ICON_SIZE.get()
        );

        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), pkt);
        sendAdminUpdateNotice(sp);
    }

    private static void sendAdminUpdateNotice(ServerPlayer player) {
        if (!player.hasPermissions(3)) return;
        if (!ADMIN_UPDATE_NOTICE_ENABLED) return;

        if (!UPDATE_CHECK_DONE) {
            runUpdateCheck();
        }

        if (!UPDATE_AVAILABLE) return;

        String currentVersion = CURRENT_VERSION;
        String latestVersion = RESOLVED_LATEST_VERSION;
        String downloadUrl = RESOLVED_DOWNLOAD_URL;

        UUID playerId = player.getUUID();
        String lastNotified = LAST_NOTIFIED_VERSION.get(playerId);
        if (latestVersion.equalsIgnoreCase(lastNotified)) return;

        if (downloadUrl == null) {
            downloadUrl = "";
        } else {
            downloadUrl = downloadUrl.trim();
        }

        String line1 = "[emoji=system][label=SYSTEM][box][color=#FFF200] New update is ready to install.";
        String line2 = "[emoji=system][label=SYSTEM][box][color=#FFF200] Current: " + currentVersion
                + " | Latest: " + latestVersion
                + (downloadUrl.isEmpty() ? "" : " | Download: " + downloadUrl);

        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ChannelMsgPacket(line1));
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ChannelMsgPacket(line2));
        LAST_NOTIFIED_VERSION.put(playerId, latestVersion);
    }

    private static void runUpdateCheck() {
        String currentVersion = ModList.get()
                .getModContainerById(FiveMHudMod.MODID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");

        UpdateInfo info = fetchLatestFromCurseForge();
        String latestVersion = info.latestVersion;

        CURRENT_VERSION = currentVersion;
        RESOLVED_LATEST_VERSION = latestVersion;
        RESOLVED_DOWNLOAD_URL = info.downloadUrl;
        UPDATE_AVAILABLE = !latestVersion.isEmpty() && isNewerVersion(latestVersion, currentVersion);
        UPDATE_CHECK_DONE = true;
    }

    private static UpdateInfo fetchLatestFromCurseForge() {
        try {
            String html = httpGet(CURSEFORGE_FILES_URL);
            if (html.isEmpty()) return UpdateInfo.empty();

            String fileId = extractFirst(html, "\\/minecraft\\/mc-mods\\/roleplay-chats\\/files\\/(\\d+)");
            String version = extractLatestVersion(html);

            if (version.isEmpty()) return UpdateInfo.empty();

            String downloadUrl = fileId.isEmpty() ? "" : (CURSEFORGE_FILE_URL_PREFIX + fileId + "/download");
            return new UpdateInfo(version, downloadUrl);
        } catch (Exception ex) {
            FiveMHudMod.LOGGER.warn("[BetterMChats] Update check failed from CurseForge: {}", ex.getMessage());
            return UpdateInfo.empty();
        }
    }

    private static String extractLatestVersion(String html) {
        String[] patterns = new String[] {
                "\\\"displayName\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"",
                "\\\"fileName\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"",
                "Roleplay\\s*Chats[^0-9]*(\\d+\\.\\d+(?:\\.\\d+)*)"
        };

        for (String p : patterns) {
            Matcher m = Pattern.compile(p).matcher(html);
            while (m.find()) {
                String candidate = m.group(1);
                String version = extractVersionToken(candidate);
                if (!version.isEmpty()) return version;
            }
        }

        return "";
    }

    private static String extractVersionToken(String text) {
        if (text == null) return "";
        Matcher m = Pattern.compile("(\\d+\\.\\d+(?:\\.\\d+)*)").matcher(text);
        if (m.find()) return m.group(1);
        return "";
    }

    private static String extractFirst(String text, String regex) {
        if (text == null) return "";
        Matcher m = Pattern.compile(regex).matcher(text);
        if (m.find()) return m.group(1);
        return "";
    }

    private static String httpGet(String urlText) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlText).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(HTTP_TIMEOUT_MS);
        conn.setReadTimeout(HTTP_TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "BetterMChats-UpdateChecker/1.0");

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
