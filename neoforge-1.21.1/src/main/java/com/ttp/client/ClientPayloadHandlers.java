package com.ttp.client;

import com.ttp.marker.QueueSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.Map;
import java.util.UUID;

public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {}

    public static void placeResult(boolean success, Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(message, true);
            if (success) {
                mc.player.playSound(SoundEvents.ARROW_HIT_PLAYER, 1.0F, 1.0F);
            }
        }
    }

    public static void ownQueue(QueueSlot[] slots) {
        ClientMarkerData.applyOwnQueue(slots);
    }

    public static void tempFlags(Map<UUID, Boolean> flags) {
        ClientMarkerData.setTempFlags(flags);
    }
}
