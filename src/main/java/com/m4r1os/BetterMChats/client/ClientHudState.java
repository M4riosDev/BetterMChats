package com.m4r1os.BetterMChats.client;

public class ClientHudState {

    // anchor: 0=TOP_RIGHT, 1=TOP_LEFT, 2=BOTTOM_RIGHT, 3=BOTTOM_LEFT
    public static volatile int anchor = 1; 

    public static volatile int offsetX = 8;
    public static volatile int offsetY = 8;

    public static volatile int width = 240;
    public static volatile int lineHeight = 18;
    public static volatile int gap = 4;

    public static volatile int maxEntries = 8;
    public static volatile int lifeMs = 9000;
    public static volatile int fadeMs = 1200;

    public static volatile boolean showIcon = true;
    public static volatile int iconSize = 14;

    public static void apply(int anchor, int offsetX, int offsetY, int width, int lineHeight, int gap,
                             int maxEntries, int lifeMs, int fadeMs, boolean showIcon, int iconSize) {
        ClientHudState.anchor = 1;
        ClientHudState.offsetX = clamp(offsetX, 0, 500);
        ClientHudState.offsetY = clamp(offsetY, 0, 500);
        ClientHudState.width = clamp(width, 120, 260);
        ClientHudState.lineHeight = clamp(lineHeight, 12, 40);
        ClientHudState.gap = clamp(gap, 0, 30);
        ClientHudState.maxEntries = clamp(maxEntries, 1, 20);
        ClientHudState.lifeMs = clamp(lifeMs, 500, 60000);
        ClientHudState.fadeMs = clamp(fadeMs, 0, 10000);
        ClientHudState.showIcon = showIcon;
        ClientHudState.iconSize = clamp(iconSize, 8, 32);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
