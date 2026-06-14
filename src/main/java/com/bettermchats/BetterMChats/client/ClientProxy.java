package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.util.IDistClientProxy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientProxy implements IDistClientProxy {
    @Override
    public void handleMeAboveHead(UUID playerId, String text, int durationTicks) {
        MeAboveHeadRenderer.put(playerId, text, durationTicks);
    }
}
