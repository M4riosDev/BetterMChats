package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.client.Minecraft;
import java.util.Set;

@EventBusSubscriber(modid = FiveMHudMod.MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage().trim();
        
        if (message.startsWith("/hudmute ")) {
            event.setCanceled(true);
            String channel = message.substring(9).trim();
            if (channel.isEmpty()) {
                showUsage();
                return;
            }
            MuteManager.muteChannel(channel);
            Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("§a[BetterMChats] Muted: " + channel.toUpperCase()));
            return;
        }
        
        if (message.startsWith("/hudunmute ")) {
            event.setCanceled(true);
            String channel = message.substring(11).trim();
            if (channel.isEmpty()) {
                showUsage();
                return;
            }
            MuteManager.unmuteChannel(channel);
            Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("§a[BetterMChats] Unmuted: " + channel.toUpperCase()));
            return;
        }
        
        if (message.startsWith("/hudmutedsz") || message.equals("/hudmuted")) {
            event.setCanceled(true);
            Set<String> mutedChannels = MuteManager.getMutedChannels();
            if (mutedChannels.isEmpty()) {
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("§c[BetterMChats] No channels muted."));
            } else {
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("§b[BetterMChats] Muted channels: " + String.join(", ", mutedChannels)));
            }
            return;
        }
    }

    private static void showUsage() {
        Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("§c/hudmute <channel> - Mute a channel"));
        Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("§c/hudunmute <channel> - Unmute a channel"));
        Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("§c/hudmuted - List all muted channels"));
    }
}
