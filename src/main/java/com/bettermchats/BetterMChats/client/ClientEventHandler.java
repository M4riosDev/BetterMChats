package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Set;

@EventBusSubscriber(modid = FiveMHudMod.MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage().trim();

        if (message.startsWith("/hudmute ")) {
            event.setCanceled(true);
            String channel = message.substring(9).trim();
            if (channel.isEmpty()) { showUsage(); return; }
            MuteManager.muteChannel(channel);
            sendMsg("§a[BetterMChats] Muted: " + channel.toUpperCase());
            return;
        }

        if (message.startsWith("/hudunmute ")) {
            event.setCanceled(true);
            String channel = message.substring(11).trim();
            if (channel.isEmpty()) { showUsage(); return; }
            MuteManager.unmuteChannel(channel);
            sendMsg("§a[BetterMChats] Unmuted: " + channel.toUpperCase());
            return;
        }

        if (message.startsWith("/hudmutedsz") || message.equals("/hudmuted")) {
            event.setCanceled(true);
            Set<String> muted = MuteManager.getMutedChannels();
            if (muted.isEmpty()) {
                sendMsg("§c[BetterMChats] No channels muted.");
            } else {
                sendMsg("§b[BetterMChats] Muted channels: " + String.join(", ", muted));
            }
        }
    }

    private static void sendMsg(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(new StringTextComponent(text), null); // 1.16.5: sendMessage(comp, uuid)
        }
    }

    private static void showUsage() {
        sendMsg("§c/hudmute <channel> - Mute a channel");
        sendMsg("§c/hudunmute <channel> - Unmute a channel");
        sendMsg("§c/hudmuted - List all muted channels");
    }
}
