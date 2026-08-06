package com.bettermchats.BetterMChats;

import com.bettermchats.BetterMChats.commands.ModCommands;
import com.bettermchats.BetterMChats.config.ServerHudConfig;
import com.bettermchats.BetterMChats.network.ModNetwork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(FiveMHudMod.MODID)
public class FiveMHudMod {
    public static final Logger LOGGER = LogManager.getLogger("FiveMHud");
    public static final String MODID = "bettermchats";

    public FiveMHudMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[BetterMChats] Opened");

        modEventBus.addListener(ModNetwork::register);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerHudConfig.SPEC);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent e) {
        ModCommands.register(e.getDispatcher());
    }
}
