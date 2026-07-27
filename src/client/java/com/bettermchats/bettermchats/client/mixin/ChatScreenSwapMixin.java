package com.bettermchats.bettermchats.client.mixin;

import com.bettermchats.bettermchats.client.FiveMChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftClient.class)
public class ChatScreenSwapMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void bettermchats$swapChatScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof ChatScreen && !(screen instanceof FiveMChatScreen)) {
            ci.cancel();
            ((MinecraftClient) (Object) this).setScreen(new FiveMChatScreen());
        }
    }
}
