package com.bettermchats.BetterMChats.commands;

import com.bettermchats.BetterMChats.network.ChannelMsgPacket;
import com.bettermchats.BetterMChats.network.ClearChatPacket;
import com.bettermchats.BetterMChats.network.MeAboveHeadPacket;
import com.bettermchats.BetterMChats.network.ModNetwork;
import com.bettermchats.BetterMChats.util.RateLimiter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.network.PacketDistributor;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ModCommands {

    private static final List<String> REGISTERED_GROUPS = new ArrayList<>();
    private static final RateLimiter RATE_LIMITER = new RateLimiter();

    /** P2 FIX: Cooldown for /me (ms). Matches a typical channel cooldown. */
    private static final long ME_COOLDOWN_MS = 3000L;
    
    private static final Map<String, Long> CHANNEL_COOLDOWNS = new HashMap<>();
    
    static {
        CHANNEL_COOLDOWNS.put("ooc", 3000L);        
        CHANNEL_COOLDOWNS.put("twt", 3000L);       
        CHANNEL_COOLDOWNS.put("staff", 1000L);     
        CHANNEL_COOLDOWNS.put("police", 2000L);    
        CHANNEL_COOLDOWNS.put("gov", 2000L);        
        CHANNEL_COOLDOWNS.put("system", 1000L);    
        CHANNEL_COOLDOWNS.put("announce", 5000L);   
        CHANNEL_COOLDOWNS.put("robbery", 2000L);    
        CHANNEL_COOLDOWNS.put("anon", 3000L);       
        CHANNEL_COOLDOWNS.put("event", 3000L);      
        CHANNEL_COOLDOWNS.put("ems", 2000L);        
        CHANNEL_COOLDOWNS.put("ad", 5000L);        
    }

    private static final SuggestionProvider<CommandSourceStack> GROUP_SUGGESTIONS = (ctx, builder) -> {
        for (String group : REGISTERED_GROUPS) {
            builder.suggest(group);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (ctx, builder) -> {
        MinecraftServer server = ctx.getSource().getServer();
        if (server != null) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                builder.suggest(p.getName().getString());
            }
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> d) {

        d.register(Commands.literal("me")
                .requires(src -> src.hasPermission(0))
                .then(Commands.argument("action", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String action = StringArgumentType.getString(ctx, "action");
                            if (action == null) action = "";
                            action = action.trim();
                            if (action.isEmpty()) return 0;

                            // P1 FIX: Explicit length guard before packet serialization.
                            if (action.length() > MeAboveHeadPacket.MAX_TEXT_LENGTH) {
                                ctx.getSource().sendSuccess(
                                    new TextComponent("[BetterMChats] Action message too long (max " + MeAboveHeadPacket.MAX_TEXT_LENGTH + " characters)."), false);
                                return 0;
                            }

                            // P2 FIX: Rate-limit /me the same way chat channels are rate-limited.
                            if (!RATE_LIMITER.canMessage(player.getUUID(), "me", ME_COOLDOWN_MS)) {
                                long remaining = RATE_LIMITER.getRemainingCooldown(player.getUUID(), "me", ME_COOLDOWN_MS);
                                int seconds = (int) Math.ceil(remaining / 1000.0);
                                ctx.getSource().sendSuccess(
                                    new TextComponent("[BetterMChats] You must wait " + seconds + " second(s) before sending another /me message."), false);
                                return 0;
                            }

                            int durationTicks = 100;
                            ModNetwork.CHANNEL.send(
                                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                                    new MeAboveHeadPacket(player.getUUID(), action, durationTicks)
                            );

                            return 1;
                        })));

        d.register(Commands.literal("hud")
                .requires(src -> src.hasPermission(0))
                .then(Commands.argument("raw", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            sendToAll(ctx.getSource().getServer(),
                                    StringArgumentType.getString(ctx, "raw"));
                            ctx.getSource().sendSuccess(
                                    new TextComponent("[BetterMChats] sent HUD raw."), false);
                            return 1;
                        })));

                    d.register(Commands.literal("clear")
                        .requires(src -> src.hasPermission(3))
                        .then(Commands.literal("chats")
                            .then(Commands.literal("all")
                                .executes(ctx -> {
                                    int cleared = clearChatsAll(ctx.getSource().getServer());
                                    ctx.getSource().sendSuccess(
                                        new TextComponent("[BetterMChats] cleared chats for all players (" + cleared + ")."),
                                        false);
                                    return 1;
                                }))
                            .then(Commands.literal("group")
                                .then(Commands.argument("group", StringArgumentType.word())
                                    .suggests(GROUP_SUGGESTIONS)
                                    .executes(ctx -> {
                                        String group = StringArgumentType.getString(ctx, "group");
                                        return runClearByRole(ctx.getSource(), group);
                                    })))
                            .then(Commands.literal("user")
                                .then(Commands.argument("username", StringArgumentType.word())
                                    .suggests(PLAYER_SUGGESTIONS)
                                    .executes(ctx -> {
                                        String username = StringArgumentType.getString(ctx, "username");
                                        return runClearByUsername(ctx.getSource(), username);
                                    })))));


        addChannel(d, "staff",    "staff",    "STAFF",    "#7D3CFF", true,  2);
        addChannel(d, "police",   "police",   "POLICE",   "#2E6BFF", false, 0);
        addChannel(d, "gov",      "gov",      "GOV",      "#2ECC71", false, 0);
        addChannel(d, "twt",      "twitter",  "TWITTER",  "#1DA1F2", false, 0);
        addChannel(d, "system",   "system",   "SYSTEM",   "#FFF200", true,  2);
        addChannel(d, "announce", "announce", "ANNOUNCE", "#34E8EB", false, 0);
        addChannel(d, "ooc",      "ooc",      "OOC",      "#2B2B2B", false, 0);
        addChannel(d, "robbery",  "robbery",  "ROBBERY",  "#FFA600", false, 0);
        addChannel(d, "anon",     "anon",     "ANONYMOUS","#FF1100", false, 0);
        addChannel(d, "event",    "event",    "EVENT",    "#9B59B6", false, 0);
        addChannel(d, "ems",      "ems",      "EMS",      "#EB6363", false, 0);
        addChannel(d, "ad",       "ad",       "ADVERTISEMENT",    "#00FF15", false, 0);
    }


    private static void addChannel(CommandDispatcher<CommandSourceStack> d,
                                   String cmd,
                                   String iconKey,
                                   String label,
                                   String colorHex,
                                   boolean staffOnly,
                                   int staffPermLevel) {

        REGISTERED_GROUPS.add(cmd);

        d.register(Commands.literal(cmd)
                .requires(src -> src.hasPermission(staffOnly ? staffPermLevel : 0))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {

                            String msg = StringArgumentType.getString(ctx, "message");
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Long cooldown = CHANNEL_COOLDOWNS.getOrDefault(cmd, 3000L);
                            
                            if (!RATE_LIMITER.canMessage(player.getUUID(), cmd, cooldown)) {
                                long remaining = RATE_LIMITER.getRemainingCooldown(player.getUUID(), cmd, cooldown);
                                int seconds = (int) Math.ceil(remaining / 1000.0);
                                ctx.getSource().sendSuccess(
                                    new TextComponent("[BetterMChats] You must wait " + seconds + " second(s) before sending another /" + cmd + " message."),
                                    false);
                                return 0;
                            }

                            String sender = ctx.getSource().getTextName();
                            if (label.equalsIgnoreCase("TWITTER") || label.equalsIgnoreCase("OOC")) {
                                msg = sender + "|" + msg;
                            }

                            String raw =
                                    "[emoji=" + iconKey + "]" +
                                    "[label=" + label + "]" +
                                    "[box]" +
                                    "[color=" + colorHex + "] " +
                                    msg;

                            MinecraftServer server = ctx.getSource().getServer();

                            if (staffOnly) {
                                sendToStaff(server, raw, staffPermLevel);
                            } else {
                                sendToAll(server, raw);
                            }

                            ctx.getSource().sendSuccess(
                                    new TextComponent("[BetterMChats] sent /" + cmd + "."), false);

                            return 1;
                        })));
    }


    private static void sendToAll(MinecraftServer server, String raw) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> p),
                    new ChannelMsgPacket(raw)
            );
        }
    }


    private static void sendToStaff(MinecraftServer server, String raw, int permLevel) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.hasPermissions(permLevel)) {
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> p),
                        new ChannelMsgPacket(raw)
                );
            }
        }
    }

    private static int clearChatsAll(MinecraftServer server) {
        int count = 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> p),
                    new ClearChatPacket()
            );
            count++;
        }
        return count;
    }

    private static int clearChatsByRole(MinecraftServer server, String roleRaw) {
        if (roleRaw == null) return -1;
        String role = roleRaw.trim().toLowerCase(Locale.ROOT);

        if (!role.equals("admin") && !role.equals("staff") && !role.equals("user")) {
            return -2;
        }

        int count = 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (matchesRole(p, role)) {
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> p),
                        new ClearChatPacket()
                );
                count++;
            }
        }
        
        return count;
    }

    private static int runClearByRole(CommandSourceStack source, String role) {
        int cleared = clearChatsByRole(source.getServer(), role);
        if (cleared == -2) {
            source.sendFailure(new TextComponent("[BetterMChats] invalid role. Use: admin, staff, user."));
            return 0;
        }
        if (cleared == 0) {
            source.sendFailure(new TextComponent("[BetterMChats] no players found with role '" + role + "'."));
            return 0;
        }

        source.sendSuccess(
                new TextComponent("[BetterMChats] cleared chats for role '" + role + "' (" + cleared + ")."),
                false);
        return 1;
    }

    private static int runClearByUsername(CommandSourceStack source, String username) {
        int cleared = clearChatsByUsername(source.getServer(), username);
        if (cleared < 0) {
            source.sendFailure(new TextComponent("[BetterMChats] player not found: " + username));
            return 0;
        }

        source.sendSuccess(
                new TextComponent("[BetterMChats] cleared chats for player '" + username + "' (" + cleared + ")."),
                false);
        return 1;
    }

    private static int clearChatsByUsername(MinecraftServer server, String username) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(username)) {
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> p),
                        new ClearChatPacket()
                );
                return 1;
            }
        }
        return -1;
    }

    private static boolean matchesRole(ServerPlayer p, String role) {
        if ("admin".equals(role)) {
            return p.hasPermissions(3) && !p.hasPermissions(4);
        }
        if ("staff".equals(role)) {
            return p.hasPermissions(2) && !p.hasPermissions(3);
        }
        if ("user".equals(role)) {
            return !p.hasPermissions(2);
        }
        return false;
    }

}
