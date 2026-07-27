package com.bettermchats.bettermchats.client.mixin;

import com.bettermchats.bettermchats.client.MeAboveHeadRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererNameplateMixin<T extends Entity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void bettermchats$onRenderLabel(T entity, Text text, MatrixStack matrices,
                                             VertexConsumerProvider vertexConsumers, int light, float tickDelta,
                                             CallbackInfo ci) {
        MeAboveHeadRenderer.onRenderLabel(entity, matrices, vertexConsumers, light);
    }
}
