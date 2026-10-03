package com.ttp.net;

import com.ttp.marker.MarkerStore;
import com.ttp.marker.QueueSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import com.ttp.TTP;

public class RenameQueueC2S implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RenameQueueC2S> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TTP.MOD_ID, "rename_queue_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RenameQueueC2S> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> payload.encode(buf), RenameQueueC2S::new);

    @Override
    public CustomPacketPayload.Type<RenameQueueC2S> type() {
        return TYPE;
    }

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

    public void handle(IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        ctx.enqueueWork(() -> {
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

    }
}
