package com.bettermchats.bettermchats.network;

import com.bettermchats.bettermchats.BetterMChats;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class ChannelMsgPacket implements CustomPayload {
    public static final CustomPayload.Id<ChannelMsgPacket> ID =
            new CustomPayload.Id<>(Identifier.of(BetterMChats.MODID, "channel_msg"));
    public static final PacketCodec<PacketByteBuf, ChannelMsgPacket> CODEC =
            CustomPayload.codecOf(ChannelMsgPacket::write, ChannelMsgPacket::read);

    public final String raw;

    public ChannelMsgPacket(String raw) {
        this.raw = raw;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(raw, 32767);
    }

    public static ChannelMsgPacket read(PacketByteBuf buf) {
        return new ChannelMsgPacket(buf.readString(32767));
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
