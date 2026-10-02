package com.ttp.net;

import com.ttp.marker.Marker;
import com.ttp.marker.MarkerStore;
import com.ttp.marker.QueueSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlaceMarkerC2S {

    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final Direction face;
    private final boolean queue;
    private final int replaceSlot;

    public PlaceMarkerC2S(int blockX, int blockY, int blockZ, Direction face, boolean queue, int replaceSlot) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.face = face;
        this.queue = queue;
        this.replaceSlot = replaceSlot;
    }

    public PlaceMarkerC2S(FriendlyByteBuf buf) {
        this.blockX = buf.readVarInt();
        this.blockY = buf.readVarInt();
        this.blockZ = buf.readVarInt();
        this.face = buf.readEnum(Direction.class);
        this.queue = buf.readBoolean();
        this.replaceSlot = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.blockX);
        buf.writeVarInt(this.blockY);
        buf.writeVarInt(this.blockZ);
        buf.writeEnum(this.face);
        buf.writeBoolean(this.queue);
        buf.writeVarInt(this.replaceSlot);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (player == null) {
                return;
            }
            BlockPos aimed = new BlockPos(this.blockX, this.blockY, this.blockZ);
            if (!MarkerStore.isWithinReach(player, aimed)) {
                sendResult(player, false, Component.translatable("ttp.msg.mark_fail").withStyle(ChatFormatting.RED));
                return;
            }
            ServerLevel level = player.serverLevel();
            if (level.getBlockState(aimed).isAir()) {
                sendResult(player, false, Component.translatable("ttp.msg.mark_fail").withStyle(ChatFormatting.RED));
                return;
            }
            BlockPos dest = MarkerStore.offsetFromFace(aimed, this.face);
            ResourceKey<Level> dim = level.dimension();
            Marker marker = new Marker(dim, dest);

            if (!this.queue) {
                MarkerStore.setTemp(player, marker);
                Component msg = Component.translatable("ttp.msg.mark_success",
                        dim.location().toString(), dest.getX(), dest.getY(), dest.getZ())
                        .withStyle(ChatFormatting.GREEN);
                sendResult(player, true, msg);
                return;
            }

            QueueSlot[] slots = MarkerStore.getQueue(player);
            int slot = this.replaceSlot;
            if (slot < 0) {
                slot = MarkerStore.firstEmptySlot(slots);
            }
            if (slot < 0 || slot >= QueueSlot.SLOT_COUNT) {
                sendResult(player, false, Component.translatable("ttp.msg.mark_queue_full").withStyle(ChatFormatting.RED));
                return;
            }
            slots[slot].setMarker(marker);
            slots[slot].setName("");
            MarkerStore.saveQueue(player, slots);
            MarkerStore.syncOwnQueue(player);
            Component msg = Component.translatable("ttp.msg.mark_success",
                    dim.location().toString(), dest.getX(), dest.getY(), dest.getZ())
                    .withStyle(ChatFormatting.GREEN);
            sendResult(player, true, msg);
        });
        ctx.get().setPacketHandled(true);
    }

    private static void sendResult(ServerPlayer player, boolean success, Component message) {
        Networking.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new PlaceResultS2C(success, message));
    }
}
