package com.m4r1os.fivemhud;

import com.m4r1os.fivemhud.commands.ModCommands;
import com.m4r1os.fivemhud.config.ServerHudConfig;
import com.m4r1os.fivemhud.network.ModNetwork;
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

    public static final String MODID = "m4r1os";

    public FiveMHudMod() {
        LOGGER.info("[FiveMHud] Opened");
        LOGGER.info("[FiveMHud] Replacing vanilla ChatScreen with FiveMChatScreen");

        ModNetwork.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerHudConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent e) {
        ModCommands.register(e.getDispatcher());
    }
}
