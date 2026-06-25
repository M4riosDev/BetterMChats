package com.bettermchats.BetterMChats.network;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class ClearChatPacket {

    public ClearChatPacket() {
    }

    public static void encode(ClearChatPacket msg, FriendlyByteBuf buf) {
    }

    public static ClearChatPacket decode(FriendlyByteBuf buf) {
        return new ClearChatPacket();
    }

    public static void handle(ClearChatPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            try {
                Class<?> cls = Class.forName("com.bettermchats.BetterMChats.client.HudOverlay");
                Method m = cls.getDeclaredMethod("clearAllMessages");
                m.invoke(null);
            } catch (Throwable t) {
                FiveMHudMod.LOGGER.error("[FiveMHud] Failed to clear client HUD chat", t);
            }
        });
        c.setPacketHandled(true);
    }
}