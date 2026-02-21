package com.m4r1os.fivemhud.config;

import net.minecraftforge.common.ForgeConfigSpec;


public class ServerHudConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<Anchor> ANCHOR;
    public static final ForgeConfigSpec.IntValue OFFSET_X;
    public static final ForgeConfigSpec.IntValue OFFSET_Y;

    public static final ForgeConfigSpec.IntValue WIDTH;
    public static final ForgeConfigSpec.IntValue LINE_HEIGHT;
    public static final ForgeConfigSpec.IntValue GAP;

    public static final ForgeConfigSpec.IntValue MAX_ENTRIES;
    public static final ForgeConfigSpec.IntValue LIFE_MS;
    public static final ForgeConfigSpec.IntValue FADE_MS;

    public static final ForgeConfigSpec.BooleanValue SHOW_ICON;
    public static final ForgeConfigSpec.IntValue ICON_SIZE;

    public enum Anchor {
        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("hud");

        ANCHOR = b.comment("HUD anchor position")
                .defineEnum("anchor", Anchor.TOP_RIGHT);

        OFFSET_X = b.comment("Padding from anchor (X)")
                .defineInRange("offsetX", 8, 0, 500);

        OFFSET_Y = b.comment("Padding from anchor (Y)")
                .defineInRange("offsetY", 8, 0, 500);

        WIDTH = b.comment("Box width")
                .defineInRange("width", 340, 120, 900);

        LINE_HEIGHT = b.comment("Box height")
                .defineInRange("lineHeight", 18, 12, 40);

        GAP = b.comment("Gap between boxes")
                .defineInRange("gap", 4, 0, 30);

        MAX_ENTRIES = b.comment("Max visible entries")
                .defineInRange("maxEntries", 8, 1, 20);

        LIFE_MS = b.comment("Time visible (ms)")
                .defineInRange("lifeMs", 9000, 500, 60000);

        FADE_MS = b.comment("Fade time (ms)")
                .defineInRange("fadeMs", 1200, 0, 10000);

        SHOW_ICON = b.comment("Render icon")
                .define("showIcon", true);

        ICON_SIZE = b.comment("Icon size px")
                .defineInRange("iconSize", 14, 8, 32);

        b.pop();

        SPEC = b.build();
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
