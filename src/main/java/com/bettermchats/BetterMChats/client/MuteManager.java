package com.bettermchats.BetterMChats.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class MuteManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String MUTE_FILE_NAME = "muted_channels.json";
    private static final Set<String> MUTED_CHANNELS = new HashSet<>();
    private static File configDir;
    private static boolean initialized = false;

    private static void ensureInit() {
        if (initialized) return;
        initialized = true;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return;
            configDir = new File(mc.gameDir, "config/bettermchats");
            if (!configDir.exists()) configDir.mkdirs();
            loadMutedChannels();
        } catch (Exception e) {
            FiveMHudMod.LOGGER.error("[BetterMChats] Failed to initialize MuteManager", e);
        }
    }

    public static void muteChannel(String channel) {
        ensureInit();
        String normalized = channel.toUpperCase().trim();
        MUTED_CHANNELS.add(normalized);
        saveMutedChannels();
    }

    public static void unmuteChannel(String channel) {
        ensureInit();
        String normalized = channel.toUpperCase().trim();
        MUTED_CHANNELS.remove(normalized);
        saveMutedChannels();
    }

    public static void toggleMute(String channel) {
        ensureInit();
        String normalized = channel.toUpperCase().trim();
        if (MUTED_CHANNELS.contains(normalized)) unmuteChannel(normalized);
        else muteChannel(normalized);
    }

    public static boolean isChannelMuted(String channel) {
        ensureInit();
        if (channel == null || channel.isEmpty()) return false;
        return MUTED_CHANNELS.contains(channel.toUpperCase().trim());
    }

    public static Set<String> getMutedChannels() {
        ensureInit();
        return new HashSet<>(MUTED_CHANNELS);
    }

    private static void loadMutedChannels() {
        try {
            if (configDir == null) return;
            File file = new File(configDir, MUTE_FILE_NAME);
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    MuteData data = GSON.fromJson(reader, MuteData.class);
                    if (data != null && data.mutedChannels != null) {
                        MUTED_CHANNELS.clear();
                        MUTED_CHANNELS.addAll(data.mutedChannels);
                    }
                }
            }
        } catch (Exception e) {
            FiveMHudMod.LOGGER.error("[BetterMChats] Failed to load muted channels", e);
        }
    }

    private static void saveMutedChannels() {
        try {
            if (configDir == null) return;
            File file = new File(configDir, MUTE_FILE_NAME);
            MuteData data = new MuteData();
            data.mutedChannels.addAll(MUTED_CHANNELS);
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            FiveMHudMod.LOGGER.error("[BetterMChats] Failed to save muted channels", e);
        }
    }

    private static class MuteData {
        public Set<String> mutedChannels = new HashSet<>();
    }
}
