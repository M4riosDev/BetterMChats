package com.m4r1os.BetterMChats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Method;
import java.util.function.Supplier;


public class HudConfigPacket {
    public final int anchor;
    public final int offsetX;
    public final int offsetY;
    public final int width;
    public final int lineHeight;
    public final int gap;
    public final int maxEntries;
    public final int lifeMs;
    public final int fadeMs;
    public final boolean showIcon;
    public final int iconSize;

    public HudConfigPacket(int anchor, int offsetX, int offsetY, int width, int lineHeight, int gap,
                           int maxEntries, int lifeMs, int fadeMs, boolean showIcon, int iconSize) {
        this.anchor = anchor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.lineHeight = lineHeight;
        this.gap = gap;
        this.maxEntries = maxEntries;
        this.lifeMs = lifeMs;
        this.fadeMs = fadeMs;
        this.showIcon = showIcon;
        this.iconSize = iconSize;
    }

    public static void encode(HudConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.anchor);
        buf.writeVarInt(msg.offsetX);
        buf.writeVarInt(msg.offsetY);
        buf.writeVarInt(msg.width);
        buf.writeVarInt(msg.lineHeight);
        buf.writeVarInt(msg.gap);
        buf.writeVarInt(msg.maxEntries);
        buf.writeVarInt(msg.lifeMs);
        buf.writeVarInt(msg.fadeMs);
        buf.writeBoolean(msg.showIcon);
        buf.writeVarInt(msg.iconSize);
    }

    public static HudConfigPacket decode(FriendlyByteBuf buf) {
        return new HudConfigPacket(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readVarInt()
        );
    }

    public static void handle(HudConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            try {
                Class<?> cls = Class.forName("com.m4r1os.BetterMChats.client.ClientHudState");
                Method m = cls.getDeclaredMethod(
                        "apply",
                        int.class, int.class, int.class, int.class, int.class, int.class,
                        int.class, int.class, int.class, boolean.class, int.class
                );
                m.invoke(null,
                        msg.anchor, msg.offsetX, msg.offsetY, msg.width, msg.lineHeight, msg.gap,
                        msg.maxEntries, msg.lifeMs, msg.fadeMs, msg.showIcon, msg.iconSize
                );
            } catch (Throwable ignored) {
            }
        });
        c.setPacketHandled(true);
    }
}
