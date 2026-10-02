package com.ttp.net;

import com.ttp.marker.MarkerStore;
import com.ttp.marker.QueueSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RenameQueueC2S {

    private final int slot;
    private final String name;

    public RenameQueueC2S(int slot, String name) {
        this.slot = slot;
        this.name = name;
    }

    public RenameQueueC2S(FriendlyByteBuf buf) {
        this.slot = buf.readVarInt();
        this.name = buf.readUtf(QueueSlot.MAX_NAME_LEN);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.slot);
        buf.writeUtf(this.name, QueueSlot.MAX_NAME_LEN);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (player == null) {
                return;
            }
            if (this.slot < 0 || this.slot >= QueueSlot.SLOT_COUNT) {
                return;
            }
            QueueSlot[] slots = MarkerStore.getQueue(player);
            if (!slots[this.slot].isFilled()) {
                return;
            }
            slots[this.slot].setName(this.name);
            MarkerStore.saveQueue(player, slots);
            MarkerStore.syncOwnQueue(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
