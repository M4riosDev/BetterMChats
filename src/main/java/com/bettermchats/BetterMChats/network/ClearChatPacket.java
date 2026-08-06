package com.bettermchats.BetterMChats.network;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Method;

public class ClearChatPacket implements CustomPacketPayload {
    public static final Type<ClearChatPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FiveMHudMod.MODID, "clear_chat"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearChatPacket> STREAM_CODEC =
            StreamCodec.unit(new ClearChatPacket());

    public static void handle(ClearChatPacket msg, IPayloadContext context) {
        try {
            Class<?> cls = Class.forName("com.bettermchats.BetterMChats.client.HudOverlay");
            Method method = cls.getDeclaredMethod("clearAllMessages");
            method.invoke(null);
        } catch (Throwable t) {
            FiveMHudMod.LOGGER.error("[BetterMChats] Failed to clear client HUD chat", t);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
