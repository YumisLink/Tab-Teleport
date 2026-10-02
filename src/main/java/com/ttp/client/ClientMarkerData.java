package com.ttp.client;

import com.ttp.marker.QueueSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class ClientMarkerData {

    private static final QueueSlot[] OWN_QUEUE = new QueueSlot[QueueSlot.SLOT_COUNT];
    private static final Map<UUID, Boolean> TEMP_FLAGS = new HashMap<>();

    static {
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            OWN_QUEUE[i] = new QueueSlot();
        }
    }

    private ClientMarkerData() {
    }

    public static QueueSlot[] getOwnQueue() {
        return OWN_QUEUE;
    }

    public static boolean isQueueFull() {
        for (QueueSlot slot : OWN_QUEUE) {
            if (!slot.isFilled()) {
                return false;
            }
        }
        return true;
    }

    public static void applyOwnQueue(QueueSlot[] slots) {
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            OWN_QUEUE[i] = slots[i].copy();
        }
    }

    public static void setTempFlags(Map<UUID, Boolean> flags) {
        TEMP_FLAGS.clear();
        TEMP_FLAGS.putAll(flags);
    }

    public static boolean hasTemp(UUID playerId) {
        return TEMP_FLAGS.getOrDefault(playerId, false);
    }
}
