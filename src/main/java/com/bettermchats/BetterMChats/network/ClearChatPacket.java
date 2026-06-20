package com.bettermchats.BetterMChats.network;

import com.bettermchats.BetterMChats.FiveMHudMod;
import com.bettermchats.BetterMChats.client.HudOverlay;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearChatPacket {

    public ClearChatPacket() {}

    public static void encode(ClearChatPacket msg, PacketBuffer buf) {}

    public static ClearChatPacket decode(PacketBuffer buf) {
        return new ClearChatPacket();
    }

    public static void handle(ClearChatPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            FiveMHudMod.LOGGER.debug("[FiveMHud] ClearChatPacket received");
            HudOverlay.clearAllMessages();
        });
        c.setPacketHandled(true);
    }
}
