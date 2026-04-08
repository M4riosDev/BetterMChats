package com.m4r1os.BetterMChats.util;

import net.minecraftforge.fml.DistExecutor;


public final class DistProxy {
    private DistProxy() {}

    public static final IDistClientProxy CLIENT = DistExecutor.safeRunForDist(
            () -> com.m4r1os.BetterMChats.client.ClientProxy::new,
            () -> com.m4r1os.BetterMChats.util.ServerProxy::new
    );
}
