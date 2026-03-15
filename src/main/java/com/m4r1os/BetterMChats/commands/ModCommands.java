package com.m4r1os.BetterMChats.commands;

import com.m4r1os.BetterMChats.network.ChannelMsgPacket;
import com.m4r1os.BetterMChats.network.ClearChatsPacket;
import com.m4r1os.BetterMChats.network.MeAboveHeadPacket;
import com.m4r1os.BetterMChats.network.ModNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

public class ModCommands {

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


        // /clearchats all | user <name> | type <channel>
        d.register(Commands.literal("clearchats")
                .requires(src -> src.hasPermissionLevel(2))
                .then(Commands.literal("all")
                        .executes(ctx -> {
                            broadcastClear(ctx.getSource().getServer(), "all", "");
                            ctx.getSource().sendFeedback(
                                    new StringTextComponent("[m4r1os] Cleared all chats."), false);
                            return 1;
                        }))
                .then(Commands.literal("user")
                        .then(Commands.argument("username", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "username");
                                    broadcastClear(ctx.getSource().getServer(), "user", name);
                                    ctx.getSource().sendFeedback(
                                            new StringTextComponent("[m4r1os] Cleared chats for user: " + name), false);
                                    return 1;
                                })))
                .then(Commands.literal("type")
                        .then(Commands.argument("channel", StringArgumentType.word())
                                .executes(ctx -> {
                                    String channel = StringArgumentType.getString(ctx, "channel");
                                    broadcastClear(ctx.getSource().getServer(), "type", channel);
                                    ctx.getSource().sendFeedback(
                                            new StringTextComponent("[m4r1os] Cleared chats of type: " + channel), false);
                                    return 1;
                                }))));

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


    private static void broadcastClear(MinecraftServer server, String mode, String value) {
        ClearChatsPacket pkt = new ClearChatsPacket(mode, value);
        for (ServerPlayerEntity p : server.getPlayerList().getPlayers()) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> p),
                    pkt
            );
        }
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
}
