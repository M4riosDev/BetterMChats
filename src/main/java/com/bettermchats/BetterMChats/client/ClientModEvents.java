package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.FiveMHudMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = FiveMHudMod.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private static final ResourceLocation HUD_LAYER =
            ResourceLocation.fromNamespaceAndPath(FiveMHudMod.MODID, "hud");

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(MuteManager::init);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(HUD_LAYER, new LayeredDraw.Layer() {
            @Override
            public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
                HudOverlay.render(guiGraphics, deltaTracker);
            }
        });
    }
}
