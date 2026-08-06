package com.bettermchats.BetterMChats.network;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Method;
import java.util.UUID;

public class MeAboveHeadPacket implements CustomPacketPayload {
    public static final int MAX_TEXT_LENGTH = 256;
    public static final Type<MeAboveHeadPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FiveMHudMod.MODID, "me_above_head"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MeAboveHeadPacket> STREAM_CODEC =
            StreamCodec.ofMember(MeAboveHeadPacket::encode, MeAboveHeadPacket::decode);

    private final UUID playerId;
    private final String text;
    private final int durationTicks;

    public MeAboveHeadPacket(UUID playerId, String text, int durationTicks) {
        this.playerId = playerId;
        this.text = text == null ? "" : text;
        this.durationTicks = durationTicks;
    }

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeUtf(text, MAX_TEXT_LENGTH);
        buf.writeVarInt(durationTicks);
    }

    private static MeAboveHeadPacket decode(RegistryFriendlyByteBuf buf) {
        return new MeAboveHeadPacket(
                buf.readUUID(),
                buf.readUtf(MAX_TEXT_LENGTH),
                buf.readVarInt());
    }

    public static void handle(MeAboveHeadPacket msg, IPayloadContext context) {
        try {
            Class<?> cls = Class.forName("com.bettermchats.BetterMChats.client.MeAboveHeadRenderer");
            Method method = cls.getDeclaredMethod("put", UUID.class, String.class, int.class);
            method.invoke(null, msg.playerId, msg.text, msg.durationTicks);
        } catch (Throwable t) {
            FiveMHudMod.LOGGER.error("[BetterMChats] Failed to render /me text", t);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
