package com.bettermchats.bettermchats.client;

import com.bettermchats.bettermchats.network.ChannelMsgPacket;
import com.bettermchats.bettermchats.network.ClearChatPacket;
import com.bettermchats.bettermchats.network.HudConfigPacket;
import com.bettermchats.bettermchats.network.MeAboveHeadPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;


public class BetterMChatsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MuteManager.init();

        HudRenderCallback.EVENT.register(HudOverlay::render);
        ClientReceiveMessageEvents.ALLOW_CHAT.register(HudOverlay::onPlayerChat);
        ClientReceiveMessageEvents.ALLOW_GAME.register(HudOverlay::onSystemChat);
        ClientEventHandler.register();

        ClientPlayNetworking.registerGlobalReceiver(ChannelMsgPacket.ID, (packet, context) ->
                context.client().execute(() -> HudOverlay.addRawMessage(packet.raw)));

        ClientPlayNetworking.registerGlobalReceiver(HudConfigPacket.ID, (packet, context) ->
                context.client().execute(() -> ClientHudState.apply(
                        packet.anchor, packet.offsetX, packet.offsetY, packet.width, packet.lineHeight, packet.gap,
                        packet.maxEntries, packet.lifeMs, packet.fadeMs, packet.showIcon, packet.iconSize)));

        ClientPlayNetworking.registerGlobalReceiver(MeAboveHeadPacket.ID, (packet, context) ->
                context.client().execute(() -> MeAboveHeadRenderer.put(packet.playerId, packet.text, packet.durationTicks)));

        ClientPlayNetworking.registerGlobalReceiver(ClearChatPacket.ID, (packet, context) ->
                context.client().execute(HudOverlay::clearAllMessages));
    }
}
