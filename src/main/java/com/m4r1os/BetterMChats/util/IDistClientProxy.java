package com.m4r1os.BetterMChats.util;

import java.util.UUID;
 
public interface IDistClientProxy {
    void handleMeAboveHead(UUID playerId, String text, int durationTicks);
}
