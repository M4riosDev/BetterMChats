package com.bettermchats.bettermchats.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;

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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) scrollEntries += 2;
        else if (verticalAmount < 0) scrollEntries -= 2;
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 266) { scrollEntries += 10; clampScroll(); return true; }
        if (keyCode == 267) { scrollEntries -= 10; clampScroll(); return true; }
        if (keyCode == 268) { scrollEntries = Integer.MAX_VALUE; clampScroll(); return true; }
        if (keyCode == 269) { scrollEntries = 0; clampScroll(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void clampScroll() {
        List<HudOverlay.Entry> hist = HudOverlay.HISTORY;
        int max = Math.max(0, hist.size() - 1);
        if (scrollEntries < 0) scrollEntries = 0;
        if (scrollEntries > max) scrollEntries = max;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        super.render(context, mouseX, mouseY, partialTicks);
        renderHistory(context);
    }

    private void renderHistory(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        TextRenderer font = mc.textRenderer;
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

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

            HudOverlay.drawOne(context, mc, font, x, y, width, baseLineH, en.parsed, 1.0f);
            cursorY = y - mm.gap;

            if (cursorY < 8) break;
        }
    }

    public void sendMessage(String message) {
        if (message == null) return;
        String m = message.trim();
        if (m.isEmpty()) return;
        this.sendMessage(m, true);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}