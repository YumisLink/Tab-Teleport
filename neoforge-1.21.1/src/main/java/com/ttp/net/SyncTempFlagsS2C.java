package com.ttp.net;

import net.minecraft.network.FriendlyByteBuf;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import com.ttp.TTP;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SyncTempFlagsS2C implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncTempFlagsS2C> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TTP.MOD_ID, "sync_temp_flags_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTempFlagsS2C> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> payload.encode(buf), SyncTempFlagsS2C::new);

    @Override
    public CustomPacketPayload.Type<SyncTempFlagsS2C> type() {
        return TYPE;
    }

    private final Map<UUID, Boolean> flags;

    public SyncTempFlagsS2C(Map<UUID, Boolean> flags) {
        this.flags = flags;
    }

    public SyncTempFlagsS2C(FriendlyByteBuf buf) {
        this.flags = new HashMap<>();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            UUID id = buf.readUUID();
            boolean has = buf.readBoolean();
            this.flags.put(id, has);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.flags.size());
        for (var entry : this.flags.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeBoolean(entry.getValue());
        }
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.ttp.client.ClientPayloadHandlers.tempFlags(this.flags));
    }
}
