package com.bettermchats.BetterMChats.network;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Method;

public class ChannelMsgPacket implements CustomPacketPayload {
    public static final Type<ChannelMsgPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FiveMHudMod.MODID, "channel_message"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelMsgPacket> STREAM_CODEC =
            StreamCodec.ofMember(ChannelMsgPacket::encode, ChannelMsgPacket::decode);

    public final String raw;

    public ChannelMsgPacket(String raw) {
        this.raw = raw == null ? "" : raw;
    }

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(raw, 32767);
    }

    private static ChannelMsgPacket decode(RegistryFriendlyByteBuf buf) {
        return new ChannelMsgPacket(buf.readUtf(32767));
    }

    public static void handle(ChannelMsgPacket msg, IPayloadContext context) {
        try {
            Class<?> cls = Class.forName("com.bettermchats.BetterMChats.client.HudOverlay");
            Method method = cls.getDeclaredMethod("addRawMessage", String.class);
            method.invoke(null, msg.raw);
        } catch (Throwable t) {
            FiveMHudMod.LOGGER.error("[BetterMChats] Failed to deliver message to client HUD", t);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
