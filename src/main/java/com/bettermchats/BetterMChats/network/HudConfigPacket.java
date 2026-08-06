package com.bettermchats.BetterMChats.network;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Method;

public class HudConfigPacket implements CustomPacketPayload {
    public static final Type<HudConfigPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FiveMHudMod.MODID, "hud_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HudConfigPacket> STREAM_CODEC =
            StreamCodec.ofMember(HudConfigPacket::encode, HudConfigPacket::decode);

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

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(anchor);
        buf.writeVarInt(offsetX);
        buf.writeVarInt(offsetY);
        buf.writeVarInt(width);
        buf.writeVarInt(lineHeight);
        buf.writeVarInt(gap);
        buf.writeVarInt(maxEntries);
        buf.writeVarInt(lifeMs);
        buf.writeVarInt(fadeMs);
        buf.writeBoolean(showIcon);
        buf.writeVarInt(iconSize);
    }

    private static HudConfigPacket decode(RegistryFriendlyByteBuf buf) {
        return new HudConfigPacket(
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readBoolean(), buf.readVarInt());
    }

    public static void handle(HudConfigPacket msg, IPayloadContext context) {
        try {
            Class<?> cls = Class.forName("com.bettermchats.BetterMChats.client.ClientHudState");
            Method method = cls.getDeclaredMethod(
                    "apply",
                    int.class, int.class, int.class, int.class, int.class, int.class,
                    int.class, int.class, int.class, boolean.class, int.class
            );
            method.invoke(null,
                    msg.anchor, msg.offsetX, msg.offsetY, msg.width, msg.lineHeight, msg.gap,
                    msg.maxEntries, msg.lifeMs, msg.fadeMs, msg.showIcon, msg.iconSize);
        } catch (Throwable t) {
            FiveMHudMod.LOGGER.error("[BetterMChats] Failed to apply HUD config", t);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
