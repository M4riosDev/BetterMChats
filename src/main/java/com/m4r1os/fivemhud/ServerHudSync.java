package com.m4r1os.fivemhud;

import com.m4r1os.fivemhud.config.ServerHudConfig;
import com.m4r1os.fivemhud.network.HudConfigPacket;
import com.m4r1os.fivemhud.network.ModNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;


@Mod.EventBusSubscriber(modid = FiveMHudMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerHudSync {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        PlayerEntity player = e.getPlayer();
        if (player == null || player.getEntityWorld().isRemote) return;
        if (!(player instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity sp = (ServerPlayerEntity) player;

        HudConfigPacket pkt = new HudConfigPacket(
                ServerHudConfig.anchorToInt(ServerHudConfig.ANCHOR.get()),
                ServerHudConfig.OFFSET_X.get(),
                ServerHudConfig.OFFSET_Y.get(),
                ServerHudConfig.WIDTH.get(),
                ServerHudConfig.LINE_HEIGHT.get(),
                ServerHudConfig.GAP.get(),
                ServerHudConfig.MAX_ENTRIES.get(),
                ServerHudConfig.LIFE_MS.get(),
                ServerHudConfig.FADE_MS.get(),
                ServerHudConfig.SHOW_ICON.get(),
                ServerHudConfig.ICON_SIZE.get()
        );

        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), pkt);
    }
}
