package com.m4r1os.BetterMChats.network;

import com.m4r1os.BetterMChats.FiveMHudMod;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.Method;
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
            try {
                FiveMHudMod.LOGGER.debug("[FiveMHud] ChannelMsgPacket received: {}", msg.raw);
                Class<?> cls = Class.forName("com.m4r1os.BetterMChats.client.HudOverlay");
                Method m = cls.getDeclaredMethod("addRawMessage", String.class);
                m.invoke(null, msg.raw);
            } catch (Throwable t) {
                FiveMHudMod.LOGGER.error("[FiveMHud] Failed to deliver message to client HUD", t);
            }
        });
        c.setPacketHandled(true);
    }
}
