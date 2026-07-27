package com.bettermchats.bettermchats;

import com.bettermchats.bettermchats.commands.ModCommands;
import com.bettermchats.bettermchats.config.ServerHudConfig;
import com.bettermchats.bettermchats.network.BetterMChatsNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterMChats implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("FiveMHud");
    public static final String MODID = "bettermchats";

    @Override
    public void onInitialize() {
        LOGGER.info("[BetterMChats] Opened");

        BetterMChatsNetworking.registerPayloads();

        ServerHudConfig.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ModCommands.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ServerHudSync.onPlayerJoin(handler.getPlayer()));
    }
}
