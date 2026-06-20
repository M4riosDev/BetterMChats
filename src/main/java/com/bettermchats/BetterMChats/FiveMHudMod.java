package com.bettermchats.BetterMChats;

import com.bettermchats.BetterMChats.client.HudOverlay;
import com.bettermchats.BetterMChats.commands.ModCommands;
import com.bettermchats.BetterMChats.config.ServerHudConfig;
import com.bettermchats.BetterMChats.network.ModNetwork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(FiveMHudMod.MODID)
public class FiveMHudMod {
    public static final Logger LOGGER = LogManager.getLogger("FiveMHud");
    public static final String MODID = "bettermchats";

    public FiveMHudMod() {
        LOGGER.info("[BetterMChats] Opened");

        ModNetwork.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerHudConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(HudOverlay.class);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent e) {
        ModCommands.register(e.getDispatcher());
    }
}
