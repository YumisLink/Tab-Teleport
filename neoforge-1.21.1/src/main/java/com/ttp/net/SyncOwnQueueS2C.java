package com.ttp.net;

import com.ttp.marker.Marker;
import com.ttp.marker.QueueSlot;
import net.minecraft.network.FriendlyByteBuf;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import com.ttp.TTP;

public class SyncOwnQueueS2C implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncOwnQueueS2C> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TTP.MOD_ID, "sync_own_queue_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncOwnQueueS2C> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> payload.encode(buf), SyncOwnQueueS2C::new);

    @Override
    public CustomPacketPayload.Type<SyncOwnQueueS2C> type() {
        return TYPE;
    }

    private final QueueSlot[] slots;

    public SyncOwnQueueS2C(QueueSlot[] slots) {
        this.slots = new QueueSlot[QueueSlot.SLOT_COUNT];
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            this.slots[i] = slots[i].copy();
        }
    }

    public SyncOwnQueueS2C(FriendlyByteBuf buf) {
        this.slots = new QueueSlot[QueueSlot.SLOT_COUNT];
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            this.slots[i] = new QueueSlot();
            boolean filled = buf.readBoolean();
            String name = buf.readUtf(QueueSlot.MAX_NAME_LEN);
            this.slots[i].setName(name);
            if (filled) {
                String dim = buf.readUtf();
                int x = buf.readVarInt();
                int y = buf.readVarInt();
                int z = buf.readVarInt();
                var dimKey = net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        net.minecraft.resources.ResourceLocation.tryParse(dim));
                if (dimKey != null) {
                    this.slots[i].setMarker(new Marker(dimKey, new net.minecraft.core.BlockPos(x, y, z)));
                }
            }
        }
    }

    public static SyncOwnQueueS2C fromSlots(QueueSlot[] slots) {
        return new SyncOwnQueueS2C(slots);
    }

    public void encode(FriendlyByteBuf buf) {
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            QueueSlot slot = this.slots[i];
            buf.writeBoolean(slot.isFilled());
            buf.writeUtf(slot.getName(), QueueSlot.MAX_NAME_LEN);
            if (slot.isFilled() && slot.getMarker() != null) {
                Marker m = slot.getMarker();
                buf.writeUtf(m.dimension().location().toString());
                buf.writeVarInt(m.pos().getX());
                buf.writeVarInt(m.pos().getY());
                buf.writeVarInt(m.pos().getZ());
            }
        }
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.ttp.client.ClientPayloadHandlers.ownQueue(this.slots));
    }
}
