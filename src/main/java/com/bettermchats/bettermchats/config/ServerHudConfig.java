package com.bettermchats.bettermchats.config;

import com.bettermchats.bettermchats.BetterMChats;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;


public class ServerHudConfig {

    public enum Anchor {
        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    public static Anchor anchor = Anchor.TOP_RIGHT;
    public static int offsetX = 8;
    public static int offsetY = 8;

    public static int width = 340;
    public static int lineHeight = 18;
    public static int gap = 4;

    public static int maxEntries = 8;
    public static int lifeMs = 9000;
    public static int fadeMs = 1200;

    public static boolean showIcon = true;
    public static int iconSize = 14;

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("bettermchats-server.properties");

    public static synchronized void load() {
        Properties p = new Properties();

        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                p.load(in);
            } catch (IOException e) {
                BetterMChats.LOGGER.warn("[BetterMChats] Failed to read config, using defaults", e);
            }
        }

        anchor = parseAnchor(p.getProperty("anchor"), anchor);
        offsetX = clampInt(p, "offsetX", offsetX, 0, 500);
        offsetY = clampInt(p, "offsetY", offsetY, 0, 500);

        width = clampInt(p, "width", width, 120, 900);
        lineHeight = clampInt(p, "lineHeight", lineHeight, 12, 40);
        gap = clampInt(p, "gap", gap, 0, 30);

        maxEntries = clampInt(p, "maxEntries", maxEntries, 1, 20);
        lifeMs = clampInt(p, "lifeMs", lifeMs, 500, 60000);
        fadeMs = clampInt(p, "fadeMs", fadeMs, 0, 10000);

        showIcon = parseBoolean(p.getProperty("showIcon"), showIcon);
        iconSize = clampInt(p, "iconSize", iconSize, 8, 32);

        save();
    }

    private static void save() {
        Properties p = new Properties();
        p.setProperty("anchor", anchor.name());
        p.setProperty("offsetX", String.valueOf(offsetX));
        p.setProperty("offsetY", String.valueOf(offsetY));
        p.setProperty("width", String.valueOf(width));
        p.setProperty("lineHeight", String.valueOf(lineHeight));
        p.setProperty("gap", String.valueOf(gap));
        p.setProperty("maxEntries", String.valueOf(maxEntries));
        p.setProperty("lifeMs", String.valueOf(lifeMs));
        p.setProperty("fadeMs", String.valueOf(fadeMs));
        p.setProperty("showIcon", String.valueOf(showIcon));
        p.setProperty("iconSize", String.valueOf(iconSize));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                p.store(out, "BetterMChats HUD layout - synced to every client on join. "
                        + "anchor is one of TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT.");
            }
        } catch (IOException e) {
            BetterMChats.LOGGER.warn("[BetterMChats] Failed to write config", e);
        }
    }

    private static Anchor parseAnchor(String raw, Anchor fallback) {
        if (raw == null) return fallback;
        try {
            return Anchor.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String raw, boolean fallback) {
        if (raw == null) return fallback;
        if (raw.equalsIgnoreCase("true")) return true;
        if (raw.equalsIgnoreCase("false")) return false;
        return fallback;
    }

    private static int clampInt(Properties p, String key, int fallback, int min, int max) {
        String raw = p.getProperty(key);
        int value = fallback;
        if (raw != null) {
            try {
                value = Integer.parseInt(raw.trim());
            } catch (NumberFormatException ignored) {
                value = fallback;
            }
        }
        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }

    public static int anchorToInt(Anchor a) {
        switch (a) {
            case TOP_LEFT: return 1;
            case BOTTOM_RIGHT: return 2;
            case BOTTOM_LEFT: return 3;
            case TOP_RIGHT:
            default: return 0;
        }
    }
}
