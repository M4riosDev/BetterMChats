package com.m4r1os.BetterMChats.network;

import com.m4r1os.BetterMChats.FiveMHudMod;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.Method;
import java.util.function.Supplier;


public class ClearChatsPacket {
    public final String mode;
    public final String value;

    public ClearChatsPacket(String mode, String value) {
        this.mode  = mode  == null ? "all" : mode;
        this.value = value == null ? ""    : value;
    }

    public static void encode(ClearChatsPacket msg, PacketBuffer buf) {
        buf.writeString(msg.mode,  32);
        buf.writeString(msg.value, 64);
    }

    public static ClearChatsPacket decode(PacketBuffer buf) {
        return new ClearChatsPacket(buf.readString(32), buf.readString(64));
    }

    public static void handle(ClearChatsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            try {
                Class<?> cls = Class.forName("com.m4r1os.BetterMChats.client.HudOverlay");
                Method m = cls.getDeclaredMethod("clearChats", String.class, String.class);
                m.invoke(null, msg.mode, msg.value);
            } catch (Throwable t) {
                FiveMHudMod.LOGGER.error("[FiveMHud] ClearChatsPacket failed on client", t);
            }
        });
        c.setPacketHandled(true);
    }
}
