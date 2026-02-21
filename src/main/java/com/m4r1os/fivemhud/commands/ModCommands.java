package com.m4r1os.fivemhud.commands;

import com.m4r1os.fivemhud.network.ChannelMsgPacket;
import com.m4r1os.fivemhud.network.ModNetwork;
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


        addChannel(d, "staff",    "staff",    "STAFF",    "#7D3CFF", true,  2);
        addChannel(d, "police",   "police",   "POLICE",   "#2E6BFF", false, 0);
        addChannel(d, "gov",      "gov",      "GOV",      "#2ECC71", false, 0);
        addChannel(d, "twt",      "twitter",  "TWITTER",  "#1DA1F2", false, 0);
        addChannel(d, "system",   "system",   "SYSTEM",   "#F1C40F", true,  2);
        addChannel(d, "announce", "announce", "ANNOUNCE", "#FF8C00", false, 0);
        addChannel(d, "ooc",      "ooc",      "OOC",      "#2B2B2B", false, 0);
        addChannel(d, "robbery",  "robbery",  "ROBBERY",  "#E74C3C", false, 0);
        addChannel(d, "anon",     "anon",     "ANON",     "#F1C40F", false, 0);
        addChannel(d, "event",    "announce", "EVENT",    "#9B59B6", false, 0);
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
