package com.bettermchats.BetterMChats.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RateLimiter {
    private final Map<String, Map<UUID, Long>> channelLastMessageTime = new HashMap<>();
    
    public RateLimiter() {
    }

    public boolean canMessage(UUID playerUUID, String channel, long cooldownMs) {
        long now = System.currentTimeMillis();
        Map<UUID, Long> channelMap = channelLastMessageTime.computeIfAbsent(channel, k -> new HashMap<>());
        Long lastTime = channelMap.get(playerUUID);

        if (lastTime == null) {
            channelMap.put(playerUUID, now);
            return true;
        }

        if (now - lastTime >= cooldownMs) {
            channelMap.put(playerUUID, now);
            return true;
        }

        return false;
    }

    public long getRemainingCooldown(UUID playerUUID, String channel, long cooldownMs) {
        Map<UUID, Long> channelMap = channelLastMessageTime.get(channel);
        if (channelMap == null) {
            return 0;
        }
        Long lastTime = channelMap.get(playerUUID);
        if (lastTime == null) {
            return 0;
        }
        long remaining = cooldownMs - (System.currentTimeMillis() - lastTime);
        return Math.max(0, remaining);
    }
}
