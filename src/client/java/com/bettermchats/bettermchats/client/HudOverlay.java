package com.bettermchats.bettermchats.client;

import com.bettermchats.bettermchats.BetterMChats;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class HudOverlay {
    static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();
    static final List<Entry> HISTORY = new ArrayList<>();

    public static int maxHistoryEntries = 500;

    public static void clearAllMessages() {
        ENTRIES.clear();
        HISTORY.clear();
    }

    public static void addRawMessage(String raw) {
        Markup.Parsed parsed = Markup.parse(raw);
        BetterMChats.LOGGER.debug("[FiveMHud] addRawMessage raw={} parsed{{boxed={},bg=#{},label={},emoji={},text={}}}",
                raw, parsed.boxed,
                Integer.toHexString(parsed.bgColor & 0xFFFFFF),
                parsed.label, parsed.emojiKey, parsed.text);

        if (MuteManager.isChannelMuted(parsed.label)) {
            BetterMChats.LOGGER.debug("[FiveMHud] Message from muted channel '{}' - ignoring", parsed.label);
            return;
        }

        long now = System.currentTimeMillis();
        ENTRIES.add(0, new Entry(parsed, now));
        while (ENTRIES.size() > ClientHudState.maxEntries) ENTRIES.remove(ENTRIES.size() - 1);

        HISTORY.add(new Entry(parsed, now));
        while (HISTORY.size() > maxHistoryEntries) HISTORY.remove(0);
    }

    /** Registered via {@code HudRenderCallback.EVENT.register(HudOverlay::render)}. */
    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        if (mc.currentScreen instanceof FiveMChatScreen) return;

        TextRenderer font = mc.textRenderer;
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int width = Math.min(Math.min(ClientHudState.width, 260), screenWidth - 16);
        int baseLineH = ClientHudState.lineHeight;
        int offX = ClientHudState.offsetX;
        int offY = ClientHudState.offsetY;

        boolean chatOpen = (mc.currentScreen instanceof ChatScreen) || (mc.currentScreen instanceof FiveMChatScreen);

        int x;
        int yStart;
        boolean growDown;

        switch (ClientHudState.anchor) {
            case 1: // TOP_LEFT
                x = offX; yStart = offY; growDown = true; break;
            case 2: // BOTTOM_RIGHT
                x = screenWidth - width - offX; yStart = screenHeight - 48 - offY; growDown = false; break;
            case 3: // BOTTOM_LEFT
                x = offX; yStart = screenHeight - 48 - offY; growDown = false; break;
            case 0: // TOP_RIGHT
            default:
                x = screenWidth - width - offX; yStart = offY; growDown = true; break;
        }

        if (chatOpen && growDown) {
            yStart = screenHeight - 48 - offY;
            growDown = false;
        }

        long now = System.currentTimeMillis();

        if (!chatOpen) {
            ENTRIES.removeIf(en -> (now - en.createdAt) > (long) ClientHudState.lifeMs + (long) ClientHudState.fadeMs);
        }

        Iterator<Entry> it = ENTRIES.iterator();
        int cursorY = yStart;

        while (it.hasNext()) {
            Entry en = it.next();
            long age = now - en.createdAt;

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
                int usedH = drawOne(context, mc, font, slideX, cursorY, width, baseLineH, en.parsed, alpha);
                cursorY += usedH;
                if (cursorY > screenHeight - 24) break;
            } else {
                Measure mm = measure(font, slideX, width, baseLineH, en.parsed);
                int y = cursorY - mm.height;
                drawOne(context, mc, font, slideX, y, width, baseLineH, en.parsed, alpha);
                cursorY = y - mm.gap;
                if (cursorY < 8) break;
            }
        }
    }

    /** Registered via {@code ClientReceiveMessageEvents.ALLOW_CHAT.register(HudOverlay::onPlayerChat)}. */
    public static boolean onPlayerChat(Text message, SignedMessage signedMessage, GameProfile sender,
                                        MessageType.Parameters params, Instant receptionTimestamp) {
        try {
            String plain = (message == null) ? "" : message.getString();
            plain = plain.trim();
            if (plain.isEmpty()) return true;

            String lower = plain.toLowerCase();

            if (lower.contains(" issued server command")
                    || lower.contains("issued server command")
                    || lower.contains(" sent /")
                    || lower.matches(".*\\[.*\\].*sent\\s*/.*")
                    || lower.matches(".*<.*>.*sent\\s*/.*")) {
                return false;
            }

        } catch (Throwable t) {
            BetterMChats.LOGGER.warn("[FiveMHud] onPlayerChat error", t);
        }
        return true;
    }

    /** Registered via {@code ClientReceiveMessageEvents.ALLOW_GAME.register(HudOverlay::onSystemChat)}. */
    public static boolean onSystemChat(Text message, boolean overlay) {
        if (overlay) return true; // action bar messages: leave untouched, matches Forge's separate GameInfo event

        try {
            String plain = (message == null) ? "" : message.getString();
            plain = plain.trim();
            if (plain.isEmpty()) return true;

            String raw = "[emoji=system][label=SYSTEM][box][color=#F1C40F] " + plain;
            BetterMChats.LOGGER.debug("[FiveMHud] SYSTEM intercept msg={}", plain);
            addRawMessage(raw);

            return false;

        } catch (Throwable t) {
            BetterMChats.LOGGER.warn("[FiveMHud] onSystemChat error", t);
        }
        return true;
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

    static Measure measure(TextRenderer font, int x, int w, int baseLineH, Markup.Parsed p) {
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

    private static List<String> wrapLines(TextRenderer font, String text, int maxWidth) {
        if (font == null) return List.of(text != null ? text : "");
        List<String> out = new ArrayList<>();
        if (text == null) text = "";
        text = text.replace("\r", "");

        String[] paras = text.split("\n");
        for (String para : paras) {
            if (para.isEmpty()) { out.add(""); continue; }

            String[] words = para.split(" ");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                if (word.isEmpty()) continue;
                if (line.length() == 0) {
                    if (font.getWidth(word) <= maxWidth) line.append(word);
                    else hardBreakWord(font, out, word, maxWidth);
                } else {
                    String candidate = line + " " + word;
                    if (font.getWidth(candidate) <= maxWidth) {
                        line.append(" ").append(word);
                    } else {
                        out.add(line.toString());
                        line.setLength(0);
                        if (font.getWidth(word) <= maxWidth) line.append(word);
                        else hardBreakWord(font, out, word, maxWidth);
                    }
                }
            }
            if (line.length() > 0) out.add(line.toString());
        }

        if (out.isEmpty()) out.add("");
        return out;
    }

    private static void hardBreakWord(TextRenderer font, List<String> out, String word, int maxWidth) {
        StringBuilder chunk = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            chunk.append(c);
            if (font.getWidth(chunk.toString()) > maxWidth) {
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
                        if (!left.contains(" ")) { u = left; m = m.substring(colon + 1).trim(); }
                    } else if (m.startsWith("@")) {
                        int sp = m.indexOf(' ');
                        if (sp > 1 && sp < 40) { u = m.substring(1, sp).trim(); m = m.substring(sp + 1).trim(); }
                    }
                }
                if (!u.isEmpty()) {
                    return upper.equals("TWITTER") ? "TWITTER | " + u + " | " + m : "OOC | " + u + " | " + m;
                }
                return label + ": " + msg;
            }
        }
        return (!label.isEmpty()) ? (label + ": " + msg) : msg;
    }

    static int drawOne(DrawContext context, MinecraftClient mc, TextRenderer font,
                       int x, int y, int w, int baseLineH, Markup.Parsed p, float alpha) {
        int pad = 6;
        Measure m = measure(font, x, w, baseLineH, p);

        int a = (int) (alpha * 160);
        if (p.boxed) {
            int bg = (a << 24) | (p.bgColor & 0xFFFFFF);
            context.fill(x, y, x + w, y + m.height, bg);
        }

        int cursorX = x + pad;

        if (ClientHudState.showIcon && p.emojiKey != null && !p.emojiKey.isEmpty()) {
            Identifier rl = EmojiRegistry.get(p.emojiKey);
            if (rl != null) {
                int s = ClientHudState.iconSize;
                int iconY = y + Math.round((m.height - s) / 2.0f);
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
                context.drawTexture(rl, cursorX, iconY, 0, 0, s, s, 14, 14);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
        }

        int msgColor = (((int) (alpha * 255)) << 24) | 0xFFFFFF;
        int ty = y + pad;
        int lineH = Math.max(9, baseLineH);
        for (String ln : m.lines) {
            context.drawText(font, Text.literal(ln), m.textX, ty, msgColor, false);
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
}
