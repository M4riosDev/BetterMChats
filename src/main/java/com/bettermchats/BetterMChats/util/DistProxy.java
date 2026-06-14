package com.bettermchats.BetterMChats.util;

import net.minecraftforge.fml.DistExecutor;


public final class DistProxy {
    private DistProxy() {}

    public static final IDistClientProxy CLIENT = DistExecutor.safeRunForDist(
            () -> com.bettermchats.BetterMChats.client.ClientProxy::new,
            () -> com.bettermchats.BetterMChats.util.ServerProxy::new
    );
}
