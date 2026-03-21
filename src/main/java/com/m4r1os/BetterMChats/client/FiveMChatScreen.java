package com.m4r1os.BetterMChats.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) scrollEntries += 2;
        else if (delta < 0) scrollEntries -= 2;
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 266) {
            scrollEntries += 10;
            clampScroll();
            return true;
        }
        if (keyCode == 267) {
            scrollEntries -= 10;
            clampScroll();
            return true;
        }
        if (keyCode == 268) { 
            scrollEntries = Integer.MAX_VALUE;
            clampScroll();
            return true;
        }
        if (keyCode == 269) {
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
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        super.render(ms, mouseX, mouseY, partialTicks);
        renderHistory(ms);
    }

    private void renderHistory(MatrixStack ms) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        FontRenderer font = mc.fontRenderer;
        int screenW = mc.getMainWindow().getScaledWidth();
        int screenH = mc.getMainWindow().getScaledHeight();

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

            HudOverlay.drawOne(ms, mc, font, x, y, width, baseLineH, en.parsed, 1.0f);
            cursorY = y - mm.gap;

            if (cursorY < 8) break;
        }
    }


    @Override
    public void sendMessage(String message, boolean addToChat) {
        if (message == null) return;
        String m = message.trim();
        if (m.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendChatMessage(m);
        }

        try {
            if (addToChat && mc.ingameGUI != null && mc.ingameGUI.getChatGUI() != null) {
                mc.ingameGUI.getChatGUI().addToSentMessages(m);
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
