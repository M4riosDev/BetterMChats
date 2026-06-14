package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraft.util.text.StringTextComponent;
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
            Minecraft.getInstance().player.sendMessage(
                    new StringTextComponent("§a[BetterMChats] Muted: " + channel.toUpperCase()),
                    null
            );
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
            Minecraft.getInstance().player.sendMessage(
                    new StringTextComponent("§a[BetterMChats] Unmuted: " + channel.toUpperCase()),
                    null
            );
            return;
        }
        
        if (message.startsWith("/hudmutedsz") || message.equals("/hudmuted")) {
            event.setCanceled(true);
            Set<String> mutedChannels = MuteManager.getMutedChannels();
            if (mutedChannels.isEmpty()) {
                Minecraft.getInstance().player.sendMessage(
                        new StringTextComponent("§c[BetterMChats] No channels muted."),
                        null
                );
            } else {
                Minecraft.getInstance().player.sendMessage(
                        new StringTextComponent("§b[BetterMChats] Muted channels: " + String.join(", ", mutedChannels)),
                        null
                );
            }
            return;
        }
    }

    private static void showUsage() {
        Minecraft.getInstance().player.sendMessage(
                new StringTextComponent("§c/hudmute <channel> - Mute a channel"),
                null
        );
        Minecraft.getInstance().player.sendMessage(
                new StringTextComponent("§c/hudunmute <channel> - Unmute a channel"),
                null
        );
        Minecraft.getInstance().player.sendMessage(
                new StringTextComponent("§c/hudmuted - List all muted channels"),
                null
        );
    }
}
