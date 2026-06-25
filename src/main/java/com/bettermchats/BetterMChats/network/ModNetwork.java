package com.bettermchats.BetterMChats.network;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;
    private static int id = 0;

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(FiveMHudMod.MODID, "main"),
                () -> PROTOCOL,
                PROTOCOL::equals,
                PROTOCOL::equals
        );

        CHANNEL.registerMessage(nextId(), ChannelMsgPacket.class,
                ChannelMsgPacket::encode,
                ChannelMsgPacket::decode,
                ChannelMsgPacket::handle
        );

        CHANNEL.registerMessage(nextId(), HudConfigPacket.class,
                HudConfigPacket::encode,
                HudConfigPacket::decode,
                HudConfigPacket::handle
        );

        CHANNEL.registerMessage(nextId(), MeAboveHeadPacket.class,
                MeAboveHeadPacket::encode,
                MeAboveHeadPacket::decode,
                MeAboveHeadPacket::handle
        );

        CHANNEL.registerMessage(nextId(), ClearChatPacket.class,
                ClearChatPacket::encode,
                ClearChatPacket::decode,
                ClearChatPacket::handle
        );
    }

    private static int nextId() {
        return id++;
    }
}