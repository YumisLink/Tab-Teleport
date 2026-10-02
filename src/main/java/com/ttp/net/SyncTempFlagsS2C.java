package com.ttp.net;

import com.ttp.client.ClientMarkerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncTempFlagsS2C {

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

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientMarkerData.setTempFlags(this.flags);
        }));
        ctx.get().setPacketHandled(true);
    }
}
