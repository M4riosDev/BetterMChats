package com.m4r1os.BetterMChats.client;

import com.m4r1os.BetterMChats.FiveMHudMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Method;
@EventBusSubscriber(modid = FiveMHudMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public class HudOverlay {


    static final List<Entry> ENTRIES = new ArrayList<>();
    static final List<Entry> HISTORY = new ArrayList<>();

    public static int maxHistoryEntries = 500;

    public static void clearAllMessages() {
        ENTRIES.clear();
        HISTORY.clear();
    }

    public static void addRawMessage(String raw) {
        Markup.Parsed parsed = Markup.parse(raw);
        FiveMHudMod.LOGGER.debug("[FiveMHud] addRawMessage raw={} parsed{boxed={},bg=#{},label={},emoji={},text={}}",
                raw,
                parsed.boxed,
                Integer.toHexString(parsed.bgColor & 0xFFFFFF),
                parsed.label,
                parsed.emojiKey,
                parsed.text);
        
        if (MuteManager.isChannelMuted(parsed.label)) {
            FiveMHudMod.LOGGER.debug("[FiveMHud] Message from muted channel '{}' - ignoring", parsed.label);
            return;
        }
        
        long now = System.currentTimeMillis();
        ENTRIES.add(0, new Entry(parsed, now));
        while (ENTRIES.size() > ClientHudState.maxEntries) ENTRIES.remove(ENTRIES.size() - 1);

        HISTORY.add(new Entry(parsed, now));
        while (HISTORY.size() > maxHistoryEntries) HISTORY.remove(0);
    }

    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.screen instanceof FiveMChatScreen) return;

        PoseStack poseStack = new PoseStack();
        Font font = mc.font;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int width = Math.min(Math.min(ClientHudState.width, 260), screenW - 16);

        int baseLineH = ClientHudState.lineHeight;
        int offX = ClientHudState.offsetX;
        int offY = ClientHudState.offsetY;

        boolean chatOpen = (mc.screen instanceof ChatScreen) || (mc.screen instanceof FiveMChatScreen);

        int x;
        int yStart;
        boolean growDown;

        if (chatOpen) {
            x = offX;
            yStart = screenH - 48 - offY;
            growDown = false;
        } else {
            x = offX;
            yStart = offY;
            growDown = true;
        }

        long now = System.currentTimeMillis();
        Iterator<Entry> it = ENTRIES.iterator();

        int cursorY = yStart;

        while (it.hasNext()) {
            Entry en = it.next();
            long age = now - en.createdAt;

            if (!chatOpen) {
                if (age > (long) ClientHudState.lifeMs + (long) ClientHudState.fadeMs) {
                    it.remove();
                    continue;
                }
            }

            float alpha = 1.0f;
            if (!chatOpen) {
                if (age > ClientHudState.lifeMs && ClientHudState.fadeMs > 0) {
                    float t = (age - ClientHudState.lifeMs) / (float) ClientHudState.fadeMs;
                    alpha = 1.0f - Math.min(1.0f, t);
                }
            }

            float slideT = Math.min(1.0f, age / 200.0f);
            int slideX = x - (int) ((1.0f - slideT) * 14.0f);

            if (growDown) {
                int usedH = drawOne(poseStack, mc, font, slideX, cursorY, width, baseLineH, en.parsed, alpha);
                cursorY += usedH;
                if (cursorY > screenH - 24) break;
            } else {
                Measure mm = measure(font, slideX, width, baseLineH, en.parsed);
                int measuredH = mm.height;
                int measuredGap = mm.gap;

                int y = cursorY - measuredH;
                drawOne(poseStack, mc, font, slideX, y, width, baseLineH, en.parsed, alpha);
                cursorY = y - measuredGap;

                if (cursorY < 8) break;
            }
        }
    }

    @SubscribeEvent
    public static void onScreenOpen(GuiOpenEvent e) {
        if (e.getGui() instanceof ChatScreen && !(e.getGui() instanceof FiveMChatScreen)) {
            e.setGui(new FiveMChatScreen());
        }
    }

    @SubscribeEvent
    public static void onClientChat(ClientChatReceivedEvent e) {
        try {
            Component c = e.getMessage();
            String plain = (c == null) ? "" : c.getString();
            if (plain == null) plain = "";
            plain = plain.trim();
            if (plain.isEmpty()) return;

            String lower = plain.toLowerCase();

            if (lower.contains(" issued server command")
                    || lower.contains("issued server command")
                    || lower.contains(" sent /")
                    || lower.matches(".*\\[.*\\].*sent\\s*/.*")
                    || lower.matches(".*<.*>.*sent\\s*/.*")) {
                e.setCanceled(true);
                return;
            }

            net.minecraft.network.chat.ChatType chatType = e.getType();
            boolean isSystem = chatType == net.minecraft.network.chat.ChatType.SYSTEM
                    || chatType == net.minecraft.network.chat.ChatType.GAME_INFO;

            if (isSystem) {
                String raw = "[emoji=system][label=SYSTEM][box][color=#F1C40F] " + plain;
                FiveMHudMod.LOGGER.debug("[FiveMHud] SYSTEM intercept chatType={} msg={}", chatType, plain);
                addRawMessage(raw);
                e.setCanceled(true);
            }

        } catch (Throwable t) {
            FiveMHudMod.LOGGER.warn("[FiveMHud] onClientChat error", t);
        }
    }

    static class Measure {
        final List<String> lines;
        final int height;
        final int gap;
        final int textX;

        Measure(List<String> lines, int height, int gap, int textX) {
            this.lines = lines;
            this.height = height;
            this.gap = gap;
            this.textX = textX;
        }
    }

    static Measure measure(Font font, int x, int w, int baseLineH, Markup.Parsed p) {
        int pad = 6;
        int lineH = Math.max(9, baseLineH);

        int cursorX = x + pad;

        int iconPad = 0;
        if (ClientHudState.showIcon && p.emojiKey != null && !p.emojiKey.isEmpty() && EmojiRegistry.get(p.emojiKey) != null) {
            iconPad = ClientHudState.iconSize + 6;
        }
        cursorX += iconPad;

        String full = buildDisplayText(p);
        int maxTextW = Math.max(10, w - (pad + iconPad) - pad);

        List<String> lines = wrapLines(font, full, maxTextW);

        int height = Math.max(18, pad + (lines.size() * lineH) + pad);
        int gap = (lines.size() > 1) ? 8 : 4;

        return new Measure(lines, height, gap, cursorX);
    }

    private static List<String> wrapLines(Font font, String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        if (text == null) text = "";
        text = text.replace("\r", "");

        String[] paras = text.split("\n");
        for (String para : paras) {
            String s = para;
            if (s.isEmpty()) {
                out.add("");
                continue;
            }

            String[] words = s.split(" ");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                if (word.isEmpty()) continue;

                if (line.length() == 0) {

                    if (font.width(word) <= maxWidth) {
                        line.append(word);
                    } else {

                        hardBreakWord(font, out, word, maxWidth);
                    }
                } else {
                    String candidate = line + " " + word;
                    if (font.width(candidate) <= maxWidth) {
                        line.append(" ").append(word);
                    } else {
                        out.add(line.toString());
                        line.setLength(0);

                        if (font.width(word) <= maxWidth) {
                            line.append(word);
                        } else {
                            hardBreakWord(font, out, word, maxWidth);
                        }
                    }
                }
            }

            if (line.length() > 0) out.add(line.toString());
        }

        if (out.isEmpty()) out.add("");
        return out;
    }

    private static void hardBreakWord(Font font, List<String> out, String word, int maxWidth) {
        StringBuilder chunk = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            chunk.append(c);
            if (font.width(chunk.toString()) > maxWidth) {

                if (chunk.length() > 1) {
                    out.add(chunk.substring(0, chunk.length() - 1));
                    chunk = new StringBuilder().append(c);
                } else {

                    out.add(chunk.toString());
                    chunk.setLength(0);
                }
            }
        }
        if (chunk.length() > 0) out.add(chunk.toString());
    }

    private static String buildDisplayText(Markup.Parsed p) {
        String label = (p.label == null) ? "" : p.label.trim();
        String msg = (p.text == null) ? "" : p.text;

        if (!label.isEmpty()) {
            String upper = label.toUpperCase();
            if (upper.equals("TWITTER") || upper.equals("OOC")) {
                String u = "";
                String m = msg;

                int bar = m.indexOf('|');
                if (bar > 0 && bar < 40) {
                    u = m.substring(0, bar).trim();
                    m = m.substring(bar + 1).trim();
                } else {
                    int colon = m.indexOf(':');
                    if (colon > 0 && colon < 40) {
                        String left = m.substring(0, colon).trim();
                        if (!left.contains(" ")) {
                            u = left;
                            m = m.substring(colon + 1).trim();
                        }
                    } else if (m.startsWith("@")) {
                        int sp = m.indexOf(' ');
                        if (sp > 1 && sp < 40) {
                            u = m.substring(1, sp).trim();
                            m = m.substring(sp + 1).trim();
                        }
                    }
                }

                if (!u.isEmpty()) {
                    if (upper.equals("TWITTER")) return "TWITTER | " + u + " | " + m;
                    return "OOC | " + u + " | " + m;
                }

                return label + ": " + msg;
            }
        }

        return (!label.isEmpty()) ? (label + ": " + msg) : msg;
    }

    static int drawOne(PoseStack poseStack, Minecraft mc, Font font,
                               int x, int y, int w, int baseLineH, Markup.Parsed p, float alpha) {

        int pad = 6;

        Measure m = measure(font, x, w, baseLineH, p);

        int a = (int) (alpha * 160);
        if (p.boxed) {
            int bg = (a << 24) | (p.bgColor & 0xFFFFFF);
            GuiComponent.fill(poseStack, x, y, x + w, y + m.height, bg);
        }

        int cursorX = x + pad;

        if (ClientHudState.showIcon && p.emojiKey != null && !p.emojiKey.isEmpty()) {
            ResourceLocation rl = EmojiRegistry.get(p.emojiKey);
            if (rl == null) {
                FiveMHudMod.LOGGER.warn("[FiveMHud] Missing icon mapping for emojiKey={}", p.emojiKey);
            }
            if (rl != null) {
                RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, rl);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

                int s = ClientHudState.iconSize;
                int iconY = y + Math.round((m.height - s) / 2.0f);                
                
                blitIcon(poseStack, cursorX, iconY, s, s);
            }
        }

        int msgColor = (((int) (alpha * 255)) << 24) | 0xFFFFFF;

        int ty = y + pad;
        int lineH = Math.max(9, baseLineH);
        for (String ln : m.lines) {
            font.drawShadow(poseStack, ln, m.textX, ty, msgColor);
            ty += lineH;
        }

        return m.height + m.gap;
    }

    static class Entry {
        final Markup.Parsed parsed;
        final long createdAt;

        Entry(Markup.Parsed parsed, long createdAt) {
            this.parsed = parsed;
            this.createdAt = createdAt;
        }
    }

    private static void blitIcon(com.mojang.blaze3d.vertex.PoseStack poseStack, int x, int y, int w, int h) {
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        net.minecraft.client.gui.GuiComponent.blit(poseStack, x, y, 0, 0, w, h, 14, 14);
    }
}
