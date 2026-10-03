package com.ttp.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Component;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import com.ttp.TTP;

public class PlaceResultS2C implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlaceResultS2C> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TTP.MOD_ID, "place_result_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceResultS2C> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> payload.encode(buf), PlaceResultS2C::new);

    @Override
    public CustomPacketPayload.Type<PlaceResultS2C> type() {
        return TYPE;
    }

    private final boolean success;
    private final Component message;

    public PlaceResultS2C(boolean success, Component message) {
        this.success = success;
        this.message = message;
    }

    public PlaceResultS2C(RegistryFriendlyByteBuf buf) {
        this.success = buf.readBoolean();
        this.message = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.success);
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, this.message);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.ttp.client.ClientPayloadHandlers.placeResult(this.success, this.message));
    }
}
