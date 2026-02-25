package com.m4r1os.fivemhud.client;

import com.m4r1os.fivemhud.util.IDistClientProxy;
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
