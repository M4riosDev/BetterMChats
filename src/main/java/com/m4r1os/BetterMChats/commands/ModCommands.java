package com.m4r1os.BetterMChats.commands;

import com.m4r1os.BetterMChats.network.ChannelMsgPacket;
import com.m4r1os.BetterMChats.network.ClearChatPacket;
import com.m4r1os.BetterMChats.network.MeAboveHeadPacket;
import com.m4r1os.BetterMChats.network.ModNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class ModCommands {

    private static final List<String> REGISTERED_GROUPS = new ArrayList<>();

    private static final SuggestionProvider<CommandSource> GROUP_SUGGESTIONS = (ctx, builder) -> {
        for (String group : REGISTERED_GROUPS) {
            builder.suggest(group);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSource> PLAYER_SUGGESTIONS = (ctx, builder) -> {
        MinecraftServer server = ctx.getSource().getServer();
        if (server != null) {
            for (ServerPlayerEntity p : server.getPlayerList().getPlayers()) {
                builder.suggest(p.getName().getString());
            }
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSource> d) {

        d.register(Commands.literal("me")
                .requires(src -> src.hasPermissionLevel(0))
                .then(Commands.argument("action", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().asPlayer();
                            String action = StringArgumentType.getString(ctx, "action");
                            if (action == null) action = "";
                            action = action.trim();
                            if (action.isEmpty()) return 0;

                            int durationTicks = 100;
                            ModNetwork.CHANNEL.send(
                                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                                    new MeAboveHeadPacket(player.getUniqueID(), action, durationTicks)
                            );

                            return 1;
                        })));

        d.register(Commands.literal("hud")
                .requires(src -> src.hasPermissionLevel(0))
                .then(Commands.argument("raw", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            sendToAll(ctx.getSource().getServer(),
                                    StringArgumentType.getString(ctx, "raw"));
                            ctx.getSource().sendFeedback(
                                    new StringTextComponent("[m4r1os] sent HUD raw."), false);
                            return 1;
                        })));

                    d.register(Commands.literal("clear")
                        .requires(src -> src.hasPermissionLevel(3))
                        .then(Commands.literal("chats")
                            .then(Commands.literal("all")
                                .executes(ctx -> {
                                    int cleared = clearChatsAll(ctx.getSource().getServer());
                                    ctx.getSource().sendFeedback(
                                        new StringTextComponent("[m4r1os] cleared chats for all players (" + cleared + ")."),
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


    private static void addChannel(CommandDispatcher<CommandSource> d,
                                   String cmd,
                                   String iconKey,
                                   String label,
                                   String colorHex,
                                   boolean staffOnly,
                                   int staffPermLevel) {

        REGISTERED_GROUPS.add(cmd);

        d.register(Commands.literal(cmd)
                .requires(src -> src.hasPermissionLevel(staffOnly ? staffPermLevel : 0))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {

                            String msg = StringArgumentType.getString(ctx, "message");

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

                            ctx.getSource().sendFeedback(
                                    new StringTextComponent("[m4r1os] sent /" + cmd + "."), false);

                            return 1;
                        })));
    }


    private static void sendToAll(MinecraftServer server, String raw) {
        for (ServerPlayerEntity p : server.getPlayerList().getPlayers()) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> p),
                    new ChannelMsgPacket(raw)
            );
        }
    }


    private static void sendToStaff(MinecraftServer server, String raw, int permLevel) {
        for (ServerPlayerEntity p : server.getPlayerList().getPlayers()) {
            if (p.hasPermissionLevel(permLevel)) {
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> p),
                        new ChannelMsgPacket(raw)
                );
            }
        }
    }

    private static int clearChatsAll(MinecraftServer server) {
        int count = 0;
        for (ServerPlayerEntity p : server.getPlayerList().getPlayers()) {
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

        // Allow any role name, match by permission level or group
        int count = 0;
        for (ServerPlayerEntity p : server.getPlayerList().getPlayers()) {
            if (matchesRole(p, role)) {
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> p),
                        new ClearChatPacket()
                );
                count++;
            }
        }
        
        // If no players matched, return error
        if (count == 0) return -1;
        
        return count;
    }

    private static int runClearByRole(CommandSource source, String role) {
        int cleared = clearChatsByRole(source.getServer(), role);
        if (cleared < 0) {
            source.sendErrorMessage(new StringTextComponent("[m4r1os] invalid role. Use: admin, staff, user."));
            return 0;
        }

        source.sendFeedback(
                new StringTextComponent("[m4r1os] cleared chats for role '" + role + "' (" + cleared + ")."),
                false);
        return 1;
    }

    private static int runClearByUsername(CommandSource source, String username) {
        int cleared = clearChatsByUsername(source.getServer(), username);
        if (cleared < 0) {
            source.sendErrorMessage(new StringTextComponent("[m4r1os] player not found: " + username));
            return 0;
        }

        source.sendFeedback(
                new StringTextComponent("[m4r1os] cleared chats for player '" + username + "' (" + cleared + ")."),
                false);
        return 1;
    }

    private static int clearChatsByUsername(MinecraftServer server, String username) {
        for (ServerPlayerEntity p : server.getPlayerList().getPlayers()) {
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
        
        // For other group names (police, gov, etc), don't match any player
        // unless we have explicit player-to-group mapping
        return false;
    }

}
