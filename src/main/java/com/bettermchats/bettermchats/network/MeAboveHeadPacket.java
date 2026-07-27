package com.bettermchats.bettermchats.network;

import com.bettermchats.bettermchats.BetterMChats;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class MeAboveHeadPacket implements CustomPayload {
    public static final CustomPayload.Id<MeAboveHeadPacket> ID =
            new CustomPayload.Id<>(Identifier.of(BetterMChats.MODID, "me_above_head"));
    public static final PacketCodec<PacketByteBuf, MeAboveHeadPacket> CODEC =
            CustomPayload.codecOf(MeAboveHeadPacket::write, MeAboveHeadPacket::read);

    public static final int MAX_TEXT_LENGTH = 256;

    public final UUID playerId;
    public final String text;
    public final int durationTicks;

    public MeAboveHeadPacket(UUID playerId, String text, int durationTicks) {
        this.playerId = playerId;
        this.text = text;
        this.durationTicks = durationTicks;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(playerId);
        buf.writeString(text, MAX_TEXT_LENGTH);
        buf.writeVarInt(durationTicks);
    }

    public static MeAboveHeadPacket read(PacketByteBuf buf) {
        UUID id = buf.readUuid();
        String text = buf.readString(MAX_TEXT_LENGTH);
        int dur = buf.readVarInt();
        return new MeAboveHeadPacket(id, text, dur);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
