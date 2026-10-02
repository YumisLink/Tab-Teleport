package com.ttp.net;

import com.ttp.marker.Marker;
import com.ttp.marker.MarkerStore;
import com.ttp.marker.QueueSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TeleportRequestPack {

    public enum Kind {
        PLAYER,
        TEMP,
        OWN_QUEUE
    }

    private final Kind kind;
    private final String targetUUID;
    private final int queueSlot;

    public TeleportRequestPack(Kind kind, String targetUUID, int queueSlot) {
        this.kind = kind;
        this.targetUUID = targetUUID;
        this.queueSlot = queueSlot;
    }

    /** Legacy player-only constructor. */
    public TeleportRequestPack(String targetUUID) {
        this(Kind.PLAYER, targetUUID, -1);
    }

    public TeleportRequestPack(FriendlyByteBuf buf) {
        this.kind = buf.readEnum(Kind.class);
        this.targetUUID = buf.readUtf();
        this.queueSlot = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.kind);
        buf.writeUtf(this.targetUUID);
        buf.writeVarInt(this.queueSlot);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender == null) {
                return;
            }
            switch (this.kind) {
                case PLAYER -> handlePlayerTp(sender);
                case TEMP -> handleTempTp(sender);
                case OWN_QUEUE -> handleOwnQueueTp(sender);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handlePlayerTp(ServerPlayer sender) {
        ServerPlayer target = findPlayer(sender, this.targetUUID);
        if (target == null) {
            sender.displayClientMessage(
                    Component.translatable("ttp.msg.target_not_find").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (GameType.SPECTATOR == target.gameMode.getGameModeForPlayer() || !target.isAlive()) {
            sender.displayClientMessage(
                    Component.translatable("ttp.msg.target_no_safe").withStyle(ChatFormatting.RED), true);
            return;
        }
        sender.teleportTo(target.serverLevel(), target.getX(), target.getY(), target.getZ(),
                target.getYRot(), target.getXRot());
        sender.displayClientMessage(
                Component.translatable("ttp.msg.teleported").withStyle(ChatFormatting.GREEN), true);
    }

    private void handleTempTp(ServerPlayer sender) {
        UUID ownerId;
        try {
            ownerId = UUID.fromString(this.targetUUID);
        } catch (IllegalArgumentException e) {
            sender.displayClientMessage(
                    Component.translatable("ttp.msg.target_not_find").withStyle(ChatFormatting.RED), true);
            return;
        }
        ServerPlayer owner = findPlayer(sender, this.targetUUID);
        if (owner == null) {
            sender.displayClientMessage(
                    Component.translatable("ttp.msg.target_not_find").withStyle(ChatFormatting.RED), true);
            return;
        }
        Marker temp = MarkerStore.getTemp(ownerId);
        if (temp == null) {
            sender.displayClientMessage(
                    Component.translatable("ttp.msg.no_temp_marker").withStyle(ChatFormatting.RED), true);
            return;
        }
        teleportToMarker(sender, temp);
    }

    private void handleOwnQueueTp(ServerPlayer sender) {
        if (this.queueSlot < 0 || this.queueSlot >= QueueSlot.SLOT_COUNT) {
            return;
        }
        QueueSlot[] slots = MarkerStore.getQueue(sender);
        if (!slots[this.queueSlot].isFilled() || slots[this.queueSlot].getMarker() == null) {
            sender.displayClientMessage(
                    Component.translatable("ttp.msg.mark_empty_slot").withStyle(ChatFormatting.RED), true);
            return;
        }
        teleportToMarker(sender, slots[this.queueSlot].getMarker());
    }

    private void teleportToMarker(ServerPlayer sender, Marker marker) {
        ServerLevel level = MarkerStore.resolveLevel(sender.getServer(), marker);
        if (level == null) {
            sender.displayClientMessage(
                    Component.translatable("ttp.msg.target_not_find").withStyle(ChatFormatting.RED), true);
            return;
        }
        sender.teleportTo(level,
                MarkerStore.teleportX(marker),
                MarkerStore.teleportY(marker),
                MarkerStore.teleportZ(marker),
                sender.getYRot(), sender.getXRot());
        sender.displayClientMessage(
                Component.translatable("ttp.msg.teleported").withStyle(ChatFormatting.GREEN), true);
    }

    private static ServerPlayer findPlayer(ServerPlayer sender, String uuidStr) {
        try {
            UUID id = UUID.fromString(uuidStr);
            for (ServerLevel level : sender.getServer().getAllLevels()) {
                var player = level.getPlayerByUUID(id);
                if (player instanceof ServerPlayer serverPlayer) {
                    return serverPlayer;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }
}
