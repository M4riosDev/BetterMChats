package com.bettermchats.BetterMChats.network;

import com.bettermchats.BetterMChats.FiveMHudMod;
import com.bettermchats.BetterMChats.client.HudOverlay;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ChannelMsgPacket {
    public final String raw;

    public ChannelMsgPacket(String raw) {
        this.raw = raw;
    }

    public static void encode(ChannelMsgPacket msg, PacketBuffer buf) {
        buf.writeString(msg.raw);
    }

    public static ChannelMsgPacket decode(PacketBuffer buf) {
        return new ChannelMsgPacket(buf.readString(32767));
    }

    public static void handle(ChannelMsgPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            FiveMHudMod.LOGGER.debug("[FiveMHud] ChannelMsgPacket received: {}", msg.raw);
            HudOverlay.addRawMessage(msg.raw);
        });
        c.setPacketHandled(true);
    }
}
