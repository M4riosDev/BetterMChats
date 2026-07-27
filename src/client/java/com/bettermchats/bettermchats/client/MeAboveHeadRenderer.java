package com.bettermchats.bettermchats.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        long now = mc.world.getTime();
        String safe = (text == null) ? "" : text.trim();
        if (safe.length() > 120) safe = safe.substring(0, 120);
        if (safe.isEmpty()) return;
        MAP.put(playerId, new Entry(safe, now + Math.max(10, durationTicks)));
    }

    private static String get(UUID playerId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return null;
        Entry e = MAP.get(playerId);
        if (e == null) return null;
        long now = mc.world.getTime();
        if (now > e.expiresAtGameTime) {
            MAP.remove(playerId);
            return null;
        }
        return e.text;
    }

    public static void onRenderLabel(Entity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!(entity instanceof PlayerEntity p)) return;

        String action = get(p.getUuid());
        if (action == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.distanceTo(p) > 64.0f) return;

        MutableText text = Text.literal("* " + action + " *").formatted(Formatting.RED);

        float y = p.getHeight() + 0.85F;
        renderLabel(text, matrices, vertexConsumers, y, light);
    }

    private static void renderLabel(Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                     float yOffset, int light) {
        MinecraftClient mc = MinecraftClient.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        TextRenderer textRenderer = mc.textRenderer;

        matrices.push();
        matrices.translate(0.0D, (double) yOffset, 0.0D);
        matrices.multiply(dispatcher.getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float x = (float) (-textRenderer.getWidth(text) / 2);
        int bg = mc.options.getTextBackgroundColor(0.25F);

        textRenderer.draw(text, x, 0f, 0xFFFFFF, false, matrix4f, vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH, bg, light);
        textRenderer.draw(text, x, 0f, 0xFFFFFF, false, matrix4f, vertexConsumers,
                TextRenderer.TextLayerType.NORMAL, 0, light);

        matrices.pop();
    }
}
