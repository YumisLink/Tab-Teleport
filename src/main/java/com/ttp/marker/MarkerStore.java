package com.ttp.marker;

import com.ttp.net.SyncOwnQueueS2C;
import com.ttp.net.SyncTempFlagsS2C;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MarkerStore {

    private static final String NBT_ROOT = "ttp_markers";
    private static final String NBT_QUEUE = "queue";

    private static final Map<UUID, Marker> TEMP = new HashMap<>();

    private MarkerStore() {
    }

    public static BlockPos offsetFromFace(BlockPos blockPos, Direction face) {
        return switch (face) {
            case UP -> blockPos.above();
            case DOWN -> blockPos.below(2);
            default -> blockPos.relative(face);
        };
    }

    public static boolean isWithinReach(ServerPlayer player, BlockPos aimedBlock) {
        Vec3 eye = player.getEyePosition();
        Vec3 blockCenter = Vec3.atCenterOf(aimedBlock);
        double reach = player.getBlockReach() + 0.5D;
        return eye.distanceToSqr(blockCenter) <= reach * reach;
    }

    public static Marker getTemp(UUID playerId) {
        return TEMP.get(playerId);
    }

    public static boolean hasTemp(UUID playerId) {
        return TEMP.containsKey(playerId);
    }

    public static void setTemp(ServerPlayer player, Marker marker) {
        TEMP.put(player.getUUID(), marker);
        broadcastTempFlags(player.getServer());
    }

    public static void clearTemp(UUID playerId, net.minecraft.server.MinecraftServer server) {
        if (TEMP.remove(playerId) != null && server != null) {
            broadcastTempFlags(server);
        }
    }

    public static QueueSlot[] getQueue(ServerPlayer player) {
        QueueSlot[] slots = new QueueSlot[QueueSlot.SLOT_COUNT];
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            slots[i] = new QueueSlot();
        }
        var root = player.getPersistentData();
        if (!root.contains(NBT_ROOT)) {
            return slots;
        }
        var ttp = root.getCompound(NBT_ROOT);
        if (!ttp.contains(NBT_QUEUE)) {
            return slots;
        }
        var list = ttp.getList(NBT_QUEUE, 10);
        for (int i = 0; i < QueueSlot.SLOT_COUNT && i < list.size(); i++) {
            slots[i].load(list.getCompound(i));
        }
        return slots;
    }

    public static void saveQueue(ServerPlayer player, QueueSlot[] slots) {
        var root = player.getPersistentData();
        var ttp = root.contains(NBT_ROOT) ? root.getCompound(NBT_ROOT) : new net.minecraft.nbt.CompoundTag();
        var list = new net.minecraft.nbt.ListTag();
        for (QueueSlot slot : slots) {
            list.add(slot.save());
        }
        ttp.put(NBT_QUEUE, list);
        root.put(NBT_ROOT, ttp);
    }

    public static int firstEmptySlot(QueueSlot[] slots) {
        for (int i = 0; i < slots.length; i++) {
            if (!slots[i].isFilled()) {
                return i;
            }
        }
        return -1;
    }

    public static void copyQueueOnClone(ServerPlayer original, ServerPlayer clone) {
        QueueSlot[] slots = getQueue(original);
        saveQueue(clone, slots);
    }

    public static void syncOwnQueue(ServerPlayer player) {
        QueueSlot[] slots = getQueue(player);
        com.ttp.net.Networking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                SyncOwnQueueS2C.fromSlots(slots));
    }

    public static void syncTempFlagsTo(ServerPlayer player) {
        Map<UUID, Boolean> flags = new HashMap<>();
        for (ServerPlayer online : player.getServer().getPlayerList().getPlayers()) {
            flags.put(online.getUUID(), hasTemp(online.getUUID()));
        }
        com.ttp.net.Networking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncTempFlagsS2C(flags));
    }

    public static void broadcastTempFlags(net.minecraft.server.MinecraftServer server) {
        Map<UUID, Boolean> flags = new HashMap<>();
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            flags.put(online.getUUID(), hasTemp(online.getUUID()));
        }
        SyncTempFlagsS2C packet = new SyncTempFlagsS2C(flags);
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            com.ttp.net.Networking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> online), packet);
        }
    }

    public static ServerLevel resolveLevel(net.minecraft.server.MinecraftServer server, Marker marker) {
        return server.getLevel(marker.dimension());
    }

    public static double teleportX(Marker marker) {
        return marker.pos().getX() + 0.5D;
    }

    public static double teleportY(Marker marker) {
        return marker.pos().getY();
    }

    public static double teleportZ(Marker marker) {
        return marker.pos().getZ() + 0.5D;
    }
}
