package com.bettermchats.bettermchats.client;

import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

import java.util.Set;


public class ClientEventHandler {

    public static void register() {
        ClientSendMessageEvents.ALLOW_COMMAND.register(ClientEventHandler::onClientCommand);
    }

    private static boolean onClientCommand(String command) {
        if (command.startsWith("hudmute ")) {
            String channel = command.substring(8).trim();
            if (channel.isEmpty()) {
                showUsage();
                return false;
            }
            MuteManager.muteChannel(channel);
            chat().addMessage(Text.literal("\u00a7a[BetterMChats] Muted: " + channel.toUpperCase()));
            return false;
        }

        if (command.startsWith("hudunmute ")) {
            String channel = command.substring(10).trim();
            if (channel.isEmpty()) {
                showUsage();
                return false;
            }
            MuteManager.unmuteChannel(channel);
            chat().addMessage(Text.literal("\u00a7a[BetterMChats] Unmuted: " + channel.toUpperCase()));
            return false;
        }

        if (command.startsWith("hudmutedsz") || command.equals("hudmuted")) {
            Set<String> mutedChannels = MuteManager.getMutedChannels();
            if (mutedChannels.isEmpty()) {
                chat().addMessage(Text.literal("\u00a7c[BetterMChats] No channels muted."));
            } else {
                chat().addMessage(Text.literal("\u00a7b[BetterMChats] Muted channels: " + String.join(", ", mutedChannels)));
            }
            return false;
        }

        return true;
    }

    private static void showUsage() {
        chat().addMessage(Text.literal("\u00a7c/hudmute <channel> - Mute a channel"));
        chat().addMessage(Text.literal("\u00a7c/hudunmute <channel> - Unmute a channel"));
        chat().addMessage(Text.literal("\u00a7c/hudmuted - List all muted channels"));
    }

    private static ChatHud chat() {
        return MinecraftClient.getInstance().inGameHud.getChatHud();
    }
}
