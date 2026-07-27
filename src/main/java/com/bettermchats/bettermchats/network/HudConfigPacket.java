package com.bettermchats.bettermchats.network;

import com.bettermchats.bettermchats.BetterMChats;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class HudConfigPacket implements CustomPayload {
    public static final CustomPayload.Id<HudConfigPacket> ID =
            new CustomPayload.Id<>(Identifier.of(BetterMChats.MODID, "hud_config"));
    public static final PacketCodec<PacketByteBuf, HudConfigPacket> CODEC =
            CustomPayload.codecOf(HudConfigPacket::write, HudConfigPacket::read);

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

    public void write(PacketByteBuf buf) {
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

    public static HudConfigPacket read(PacketByteBuf buf) {
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

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
