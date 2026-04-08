package com.m4r1os.BetterMChats.network;

import com.m4r1os.BetterMChats.util.DistProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;


public class MeAboveHeadPacket {
    private final UUID playerId;
    private final String text;
    private final int durationTicks;

    public MeAboveHeadPacket(UUID playerId, String text, int durationTicks) {
        this.playerId = playerId;
        this.text = text;
        this.durationTicks = durationTicks;
    }

    public static void encode(MeAboveHeadPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeUtf(msg.text, 256);
        buf.writeVarInt(msg.durationTicks);
    }

    public static MeAboveHeadPacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        String text = buf.readUtf(256);
        int dur = buf.readVarInt();
        return new MeAboveHeadPacket(id, text, dur);
    }

    public static void handle(MeAboveHeadPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistProxy.CLIENT.handleMeAboveHead(msg.playerId, msg.text, msg.durationTicks);
        });
        ctx.get().setPacketHandled(true);
    }
}
