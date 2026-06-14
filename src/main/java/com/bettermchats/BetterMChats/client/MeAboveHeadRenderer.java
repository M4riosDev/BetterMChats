package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.FiveMHudMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FiveMHudMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MeAboveHeadRenderer {

    private static class Entry {
        final String text;
        final long expiresAtGameTime;

        Entry(String text, long expiresAtGameTime) {
            this.text = text;
            this.expiresAtGameTime = expiresAtGameTime;
        }
    }

    private static final Map<UUID, Entry> MAP = new HashMap<>();

    public static void put(UUID playerId, String text, int durationTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long now = mc.level.getGameTime();
        String safe = (text == null) ? "" : text.trim();
        if (safe.length() > 120) safe = safe.substring(0, 120);
        if (safe.isEmpty()) return;
        MAP.put(playerId, new Entry(safe, now + Math.max(10, durationTicks)));
    }

    private static String get(UUID playerId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        Entry e = MAP.get(playerId);
        if (e == null) return null;
        long now = mc.level.getGameTime();
        if (now > e.expiresAtGameTime) {
            MAP.remove(playerId);
            return null;
        }
        return e.text;
    }

    @SubscribeEvent
    public static void onNameplate(RenderNameplateEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();

        String action = get(p.getUUID());
        if (action == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.distanceTo(p) > 64.0f) return;

        // In 1.18.2 RenderNameplateEvent uses getPoseStack() and getMultiBufferSource()
        PoseStack poseStack = e.getPoseStack();
        MultiBufferSource buffer = e.getMultiBufferSource();
        int light = e.getPackedLight();

        MutableComponent text = new TextComponent("* " + action + " *").withStyle(ChatFormatting.RED);

        float y = p.getBbHeight() + 0.85F;
        renderLabel(p, text, poseStack, buffer, y, light);
    }

    private static void renderLabel(Entity entity, net.minecraft.network.chat.Component text,
                                    PoseStack poseStack, MultiBufferSource buffer,
                                    float yOffset, int light) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Font font = mc.font;

        poseStack.pushPose();
        poseStack.translate(0.0D, (double) yOffset, 0.0D);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        float x = (float) (-font.width(text) / 2);
        float bgOpacity = (float) mc.options.textBackgroundOpacity;
        int bg = ((int) (bgOpacity * 255.0F) << 24);

        font.drawInBatch(text, x, 0f, 0xFF0000, false, poseStack.last().pose(), buffer, true, bg, light);
        font.drawInBatch(text, x, 0f, 0xFF0000, false, poseStack.last().pose(), buffer, false, 0, light);

        poseStack.popPose();
    }
}
