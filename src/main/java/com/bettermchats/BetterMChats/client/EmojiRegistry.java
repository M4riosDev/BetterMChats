package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public class EmojiRegistry {
    private static final Map<String, ResourceLocation> MAP = new HashMap<>();

    static {
        put("staff", "textures/gui/icons/staff.png");
        put("police", "textures/gui/icons/police.png");
        put("gov", "textures/gui/icons/gov.png");

        put("twitter", "textures/gui/icons/twitter.png");
        put("system", "textures/gui/icons/system.png");
        put("ems", "textures/gui/icons/ems.png");
        put("announce", "textures/gui/icons/announce.png");
        put("ooc", "textures/gui/icons/ooc.png");
        put("event", "textures/gui/icons/event.png");
       put("robbery", "textures/gui/icons/robbery.png");
       put("anon",    "textures/gui/icons/anon.png");
       put("ad",    "textures/gui/icons/ad.png");

    }

    private static void put(String key, String path) {
        MAP.put(key, new ResourceLocation(FiveMHudMod.MODID, path));
    }

    public static ResourceLocation get(String key) {
        if (key == null) return null;
        return MAP.get(key.toLowerCase());
    }
}
