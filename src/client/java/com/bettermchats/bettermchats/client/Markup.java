package com.bettermchats.bettermchats.client;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Markup {

    public static class Parsed {
        public boolean boxed = false;
        public int bgColor = 0x222222;
        public String label = "";
        public String emojiKey = "";
        public String text = "";
    }

    private static final Map<String, Integer> NAMED = new HashMap<>();
    static {
        NAMED.put("blue", 0x2E6BFF);
        NAMED.put("red", 0xE74C3C);
        NAMED.put("green", 0x2ECC71);
        NAMED.put("yellow", 0xF1C40F);
        NAMED.put("purple", 0x7D3CFF);
        NAMED.put("gray", 0x2B2B2B);
        NAMED.put("black", 0x111111);
        NAMED.put("white", 0xFFFFFF);
        NAMED.put("twitter", 0x1DA1F2);
    }

    public static Parsed parse(String raw) {
        Parsed p = new Parsed();
        if (raw == null) return p;
        String s = raw.trim();

        int i = 0;
        while (i < s.length() && s.charAt(i) == '[') {
            int end = s.indexOf(']', i);
            if (end == -1) break;
            String tag = s.substring(i + 1, end).trim();
            i = end + 1;

            if (tag.equalsIgnoreCase("box")) {
                p.boxed = true;
                continue;
            }

            int eq = tag.indexOf('=');
            if (eq == -1) continue;

            String key = tag.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            String val = tag.substring(eq + 1).trim();

            if (key.equals("emoji")) {
                p.emojiKey = val.toLowerCase(Locale.ROOT);
            } else if (key.equals("label")) {
                p.label = val;
            } else if (key.equals("color") || key.equals("bg")) {
                p.bgColor = parseColor(val);
            }
        }

        String rest = s.substring(Math.min(i, s.length())).trim();
        p.text = rest;
        return p;
    }

    private static int parseColor(String v) {
        if (v == null) return 0x222222;
        String s = v.trim().toLowerCase(Locale.ROOT);
        if (NAMED.containsKey(s)) return NAMED.get(s);

        if (s.startsWith("#")) s = s.substring(1);
        if (s.length() == 6) {
            try {
                return Integer.parseInt(s, 16) & 0xFFFFFF;
            } catch (NumberFormatException ignored) {}
        }
        return 0x222222;
    }
}
