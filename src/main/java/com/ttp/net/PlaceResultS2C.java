package com.ttp.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlaceResultS2C {

    private final boolean success;
    private final Component message;

    public PlaceResultS2C(boolean success, Component message) {
        this.success = success;
        this.message = message;
    }

    public PlaceResultS2C(FriendlyByteBuf buf) {
        this.success = buf.readBoolean();
        this.message = buf.readComponent();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.success);
        buf.writeComponent(this.message);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(this.message, true);
                if (this.success) {
                    mc.player.playSound(SoundEvents.ARROW_HIT_PLAYER, 1.0F, 1.0F);
                }
            }
        }));
        ctx.get().setPacketHandled(true);
    }
}
