package com.bettermchats.bettermchats.network;

import com.bettermchats.bettermchats.BetterMChats;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public final class ClearChatPacket implements CustomPayload {
    public static final ClearChatPacket INSTANCE = new ClearChatPacket();
    public static final CustomPayload.Id<ClearChatPacket> ID =
            new CustomPayload.Id<>(Identifier.of(BetterMChats.MODID, "clear_chat"));
    public static final PacketCodec<PacketByteBuf, ClearChatPacket> CODEC = PacketCodec.unit(INSTANCE);

    private ClearChatPacket() {}

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
