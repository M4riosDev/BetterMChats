package com.bettermchats.BetterMChats.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private static final String PROTOCOL = "3";

    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToClient(ChannelMsgPacket.TYPE, ChannelMsgPacket.STREAM_CODEC, ChannelMsgPacket::handle);
        registrar.playToClient(HudConfigPacket.TYPE, HudConfigPacket.STREAM_CODEC, HudConfigPacket::handle);
        registrar.playToClient(MeAboveHeadPacket.TYPE, MeAboveHeadPacket.STREAM_CODEC, MeAboveHeadPacket::handle);
        registrar.playToClient(ClearChatPacket.TYPE, ClearChatPacket.STREAM_CODEC, ClearChatPacket::handle);
    }
}
