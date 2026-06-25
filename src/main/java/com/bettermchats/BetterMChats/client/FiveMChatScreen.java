package com.bettermchats.BetterMChats.client;

import com.bettermchats.BetterMChats.FiveMHudMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ChatScreen;

import java.util.List;


public class FiveMChatScreen extends ChatScreen {

    private int scrollEntries = 0;

    public FiveMChatScreen() {
        super("");
    }

    @Override
    protected void init() {
        super.init();
        clampScroll();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) scrollEntries += 2;
        else if (delta < 0) scrollEntries -= 2;
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 266) { // Page Up
            scrollEntries += 10;
            clampScroll();
            return true;
        }
        if (keyCode == 267) { // Page Down
            scrollEntries -= 10;
            clampScroll();
            return true;
        }
        if (keyCode == 268) { // Home
            scrollEntries = Integer.MAX_VALUE;
            clampScroll();
            return true;
        }
        if (keyCode == 269) { // End
            scrollEntries = 0;
            clampScroll();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void clampScroll() {
        List<HudOverlay.Entry> hist = HudOverlay.HISTORY;
        int max = Math.max(0, hist.size() - 1);
        if (scrollEntries < 0) scrollEntries = 0;
        if (scrollEntries > max) scrollEntries = max;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        renderHistory(poseStack);
    }

    private void renderHistory(PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        Font font = mc.font;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int width = Math.min(Math.min(ClientHudState.width, 260), screenW - 16);
        int baseLineH = ClientHudState.lineHeight;
        int offX = ClientHudState.offsetX;
        int offY = ClientHudState.offsetY;

        int x = offX;
        int yStart = screenH - 48 - offY;

        List<HudOverlay.Entry> hist = HudOverlay.HISTORY;
        if (hist.isEmpty()) return;

        int idx = hist.size() - 1 - scrollEntries;
        if (idx < 0) idx = 0;

        int cursorY = yStart;
        for (int i = idx; i >= 0; i--) {
            HudOverlay.Entry en = hist.get(i);
            HudOverlay.Measure mm = HudOverlay.measure(font, x, width, baseLineH, en.parsed);
            int y = cursorY - mm.height;

            HudOverlay.drawOne(poseStack, mc, font, x, y, width, baseLineH, en.parsed, 1.0f);
            cursorY = y - mm.gap;

            if (cursorY < 8) break;
        }
    }

    @Override
    public void sendMessage(String message) {
        if (message == null) return;
        String m = message.trim();
        if (m.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.chat(m);
        }

        try {
            if (mc.gui != null && mc.gui.getChat() != null) {
                mc.gui.getChat().addRecentChat(m);
            }
        } catch (UnsupportedOperationException | NullPointerException e) {
            FiveMHudMod.LOGGER.warn("[BetterMChats] Could not add message to chat history", e);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
