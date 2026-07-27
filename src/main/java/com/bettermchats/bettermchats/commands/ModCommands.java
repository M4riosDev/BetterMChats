package com.bettermchats.bettermchats.commands;

import com.bettermchats.bettermchats.network.BetterMChatsNetworking;
import com.bettermchats.bettermchats.network.MeAboveHeadPacket;
import com.bettermchats.bettermchats.util.RateLimiter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ModCommands {

    private static final List<String> REGISTERED_GROUPS = new ArrayList<>();
    private static final RateLimiter RATE_LIMITER = new RateLimiter();
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

    private static final SuggestionProvider<ServerCommandSource> GROUP_SUGGESTIONS = (ctx, builder) -> {
        for (String group : REGISTERED_GROUPS) {
            builder.suggest(group);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTIONS = (ctx, builder) -> {
        MinecraftServer server = ctx.getSource().getServer();
        if (server != null) {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                builder.suggest(p.getName().getString());
            }
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> d) {

        d.register(CommandManager.literal("me")
                .requires(src -> src.hasPermissionLevel(0))
                .then(CommandManager.argument("action", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            String action = StringArgumentType.getString(ctx, "action");
                            if (action == null) action = "";
                            action = action.trim();
                            if (action.isEmpty()) return 0;

                            if (action.length() > MeAboveHeadPacket.MAX_TEXT_LENGTH) {
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("[BetterMChats] Action message too long (max " + MeAboveHeadPacket.MAX_TEXT_LENGTH + " characters)."), false);
                                return 0;
                            }

                            if (!RATE_LIMITER.canMessage(player.getUuid(), "me", ME_COOLDOWN_MS)) {
                                long remaining = RATE_LIMITER.getRemainingCooldown(player.getUuid(), "me", ME_COOLDOWN_MS);
                                int seconds = (int) Math.ceil(remaining / 1000.0);
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("[BetterMChats] You must wait " + seconds + " second(s) before sending another /me message."), false);
                                return 0;
                            }

                            int durationTicks = 100;
                            MeAboveHeadPacket packet = new MeAboveHeadPacket(player.getUuid(), action, durationTicks);

                            // Forge's PacketDistributor.TRACKING_ENTITY_AND_SELF equivalent:
                            // everyone tracking the player, plus the player themself.
                            Set<ServerPlayerEntity> recipients = new LinkedHashSet<>(PlayerLookup.tracking(player));
                            recipients.add(player);
                            for (ServerPlayerEntity recipient : recipients) {
                                BetterMChatsNetworking.sendMeAboveHead(recipient, packet);
                            }

                            return 1;
                        })));

        d.register(CommandManager.literal("hud")
                .requires(src -> src.hasPermissionLevel(0))
                .then(CommandManager.argument("raw", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            sendToAll(ctx.getSource().getServer(),
                                    StringArgumentType.getString(ctx, "raw"));
                            ctx.getSource().sendFeedback(() -> Text.literal("[BetterMChats] sent HUD raw."), false);
                            return 1;
                        })));

        d.register(CommandManager.literal("clear")
                .requires(src -> src.hasPermissionLevel(3))
                .then(CommandManager.literal("chats")
                        .then(CommandManager.literal("all")
                                .executes(ctx -> {
                                    int cleared = clearChatsAll(ctx.getSource().getServer());
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("[BetterMChats] cleared chats for all players (" + cleared + ")."),
                                            false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("group")
                                .then(CommandManager.argument("group", StringArgumentType.word())
                                        .suggests(GROUP_SUGGESTIONS)
                                        .executes(ctx -> {
                                            String group = StringArgumentType.getString(ctx, "group");
                                            return runClearByRole(ctx.getSource(), group);
                                        })))
                        .then(CommandManager.literal("user")
                                .then(CommandManager.argument("username", StringArgumentType.word())
                                        .suggests(PLAYER_SUGGESTIONS)
                                        .executes(ctx -> {
                                            String username = StringArgumentType.getString(ctx, "username");
                                            return runClearByUsername(ctx.getSource(), username);
                                        })))));


        addChannel(d, "staff", "staff", "STAFF", "#7D3CFF", true, 2);
        addChannel(d, "police", "police", "POLICE", "#2E6BFF", false, 0);
        addChannel(d, "gov", "gov", "GOV", "#2ECC71", false, 0);
        addChannel(d, "twt", "twitter", "TWITTER", "#1DA1F2", false, 0);
        addChannel(d, "system", "system", "SYSTEM", "#FFF200", true, 2);
        addChannel(d, "announce", "announce", "ANNOUNCE", "#34E8EB", false, 0);
        addChannel(d, "ooc", "ooc", "OOC", "#2B2B2B", false, 0);
        addChannel(d, "robbery", "robbery", "ROBBERY", "#FFA600", false, 0);
        addChannel(d, "anon", "anon", "ANONYMOUS", "#FF1100", false, 0);
        addChannel(d, "event", "event", "EVENT", "#9B59B6", false, 0);
        addChannel(d, "ems", "ems", "EMS", "#EB6363", false, 0);
        addChannel(d, "ad", "ad", "ADVERTISEMENT", "#00FF15", false, 0);
    }


    private static void addChannel(CommandDispatcher<ServerCommandSource> d,
                                    String cmd,
                                    String iconKey,
                                    String label,
                                    String colorHex,
                                    boolean staffOnly,
                                    int staffPermLevel) {

        REGISTERED_GROUPS.add(cmd);

        d.register(CommandManager.literal(cmd)
                .requires(src -> src.hasPermissionLevel(staffOnly ? staffPermLevel : 0))
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {

                            String msg = StringArgumentType.getString(ctx, "message");
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            Long cooldown = CHANNEL_COOLDOWNS.getOrDefault(cmd, 3000L);

                            if (!RATE_LIMITER.canMessage(player.getUuid(), cmd, cooldown)) {
                                long remaining = RATE_LIMITER.getRemainingCooldown(player.getUuid(), cmd, cooldown);
                                int seconds = (int) Math.ceil(remaining / 1000.0);
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("[BetterMChats] You must wait " + seconds + " second(s) before sending another /" + cmd + " message."),
                                        false);
                                return 0;
                            }

                            String sender = ctx.getSource().getName();
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


                            return 1;
                        })));
    }


    private static void sendToAll(MinecraftServer server, String raw) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            BetterMChatsNetworking.sendChannelMsg(p, raw);
        }
    }


    private static void sendToStaff(MinecraftServer server, String raw, int permLevel) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p.hasPermissionLevel(permLevel)) {
                BetterMChatsNetworking.sendChannelMsg(p, raw);
            }
        }
    }

    private static int clearChatsAll(MinecraftServer server) {
        int count = 0;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            BetterMChatsNetworking.sendClearChat(p);
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
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (matchesRole(p, role)) {
                BetterMChatsNetworking.sendClearChat(p);
                count++;
            }
        }

        return count;
    }

    private static int runClearByRole(ServerCommandSource source, String role) {
        int cleared = clearChatsByRole(source.getServer(), role);
        if (cleared == -2) {
            source.sendError(Text.literal("[BetterMChats] invalid role. Use: admin, staff, user."));
            return 0;
        }
        if (cleared == 0) {
            source.sendError(Text.literal("[BetterMChats] no players found with role '" + role + "'."));
            return 0;
        }


        source.sendFeedback(
                () -> Text.literal("[BetterMChats] cleared chats for role '" + role + "' (" + cleared + ")."),
                false);
        return 1;
    }

    private static int runClearByUsername(ServerCommandSource source, String username) {
        int cleared = clearChatsByUsername(source.getServer(), username);
        if (cleared < 0) {
            source.sendError(Text.literal("[BetterMChats] player not found: " + username));
            return 0;
        }

        source.sendFeedback(
                () -> Text.literal("[BetterMChats] cleared chats for player '" + username + "' (" + cleared + ")."),
                false);
        return 1;
    }

    private static int clearChatsByUsername(MinecraftServer server, String username) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p.getName().getString().equalsIgnoreCase(username)) {
                BetterMChatsNetworking.sendClearChat(p);
                return 1;
            }
        }
        return -1;
    }

    private static boolean matchesRole(ServerPlayerEntity p, String role) {
        if ("admin".equals(role)) {
            return p.hasPermissionLevel(3);
        }
        if ("staff".equals(role)) {
            return p.hasPermissionLevel(2) && !p.hasPermissionLevel(3);
        }
        if ("user".equals(role)) {
            return !p.hasPermissionLevel(2);
        }
        return false;
    }

}
