package com.ttp.marker;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = com.ttp.TTP.MOD_ID)
public class MarkerEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MarkerStore.syncOwnQueue(player);
        MarkerStore.syncTempFlagsTo(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        var server = event.getEntity().getServer();
        MarkerStore.clearTemp(event.getEntity().getUUID(), server);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer clone)) {
            return;
        }
        if (!(event.getOriginal() instanceof ServerPlayer original)) {
            return;
        }
        if (!event.isWasDeath()) {
            return;
        }
        MarkerStore.copyQueueOnClone(original, clone);
        MarkerStore.syncOwnQueue(clone);
    }
}
