package com.bettermchats.bettermchats.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;


public final class BetterMChatsNetworking {
    private BetterMChatsNetworking() {}

    /** Registers the S2C payload types. Must be called from the common entrypoint on both sides. */
    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(ChannelMsgPacket.ID, ChannelMsgPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(HudConfigPacket.ID, HudConfigPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MeAboveHeadPacket.ID, MeAboveHeadPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearChatPacket.ID, ClearChatPacket.CODEC);
    }

    public static void sendChannelMsg(ServerPlayerEntity player, String raw) {
        ServerPlayNetworking.send(player, new ChannelMsgPacket(raw));
    }

    public static void sendHudConfig(ServerPlayerEntity player, HudConfigPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendMeAboveHead(ServerPlayerEntity player, MeAboveHeadPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendClearChat(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, ClearChatPacket.INSTANCE);
    }
}
