package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.FiveMHudMod;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
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
        if (mc.world == null) return;
        long now = mc.world.getGameTime();
        String safe = (text == null) ? "" : text.trim();
        if (safe.length() > 120) safe = safe.substring(0, 120);
        if (safe.isEmpty()) return;

        MAP.put(playerId, new Entry(safe, now + Math.max(10, durationTicks)));
    }

    private static String get(UUID playerId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null) return null;
        Entry e = MAP.get(playerId);
        if (e == null) return null;
        long now = mc.world.getGameTime();
        if (now > e.expiresAtGameTime) {
            MAP.remove(playerId);
            return null;
        }
        return e.text;
    }

    @SubscribeEvent
    public static void onNameplate(RenderNameplateEvent e) {
        if (!(e.getEntity() instanceof PlayerEntity)) return;
        PlayerEntity p = (PlayerEntity) e.getEntity();

        String action = get(p.getUniqueID());
        if (action == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getDistance(p) > 64.0f) return;

        MatrixStack ms = e.getMatrixStack();
        IRenderTypeBuffer buffer = e.getRenderTypeBuffer();
        int light = e.getPackedLight();

        ITextComponent text = new StringTextComponent("* " + action + " *")
        .mergeStyle(TextFormatting.RED);

        float y = p.getHeight() + 0.85F;
        renderLabel(p, text, ms, buffer, y, light);
    }

    private static void renderLabel(Entity entity, ITextComponent text, MatrixStack ms,
                                    IRenderTypeBuffer buffer, float yOffset, int light) {
        Minecraft mc = Minecraft.getInstance();
        EntityRendererManager rm = mc.getRenderManager();
        FontRenderer fr = mc.fontRenderer;

        ms.push();
        ms.translate(0.0D, (double) yOffset, 0.0D);
        ms.rotate(rm.getCameraOrientation());
        ms.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f mat = ms.getLast().getMatrix();
        float x = (float) (-fr.getStringPropertyWidth(text) / 2);

        float bgOpacity = mc.gameSettings.getTextBackgroundOpacity(0.25F);
        int bg = ((int) (bgOpacity * 255.0F) << 24);

        fr.func_243247_a(text, x, 0, 0xFF0000, false, mat, buffer, true, bg, light);
        fr.func_243247_a(text, x, 0, 0xFF0000, false, mat, buffer, false, 0, light);

        ms.pop();
    }
}
