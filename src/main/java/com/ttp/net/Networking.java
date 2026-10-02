package com.ttp.net;

import com.ttp.TTP;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {

    private static final String VERSION = "1.1";
    public static SimpleChannel CHANNEL;

    private static int ID = 0;

    private static int nextID() {
        return ID++;
    }

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath(TTP.MOD_ID, "main"),
                () -> VERSION,
                VERSION::equals,
                VERSION::equals
        );
        CHANNEL.registerMessage(nextID(), TeleportRequestPack.class,
                TeleportRequestPack::encode,
                TeleportRequestPack::new,
                TeleportRequestPack::handle);
        CHANNEL.registerMessage(nextID(), PlaceMarkerC2S.class,
                PlaceMarkerC2S::encode,
                PlaceMarkerC2S::new,
                PlaceMarkerC2S::handle);
        CHANNEL.registerMessage(nextID(), RenameQueueC2S.class,
                RenameQueueC2S::encode,
                RenameQueueC2S::new,
                RenameQueueC2S::handle);
        CHANNEL.registerMessage(nextID(), SyncOwnQueueS2C.class,
                SyncOwnQueueS2C::encode,
                SyncOwnQueueS2C::new,
                SyncOwnQueueS2C::handle);
        CHANNEL.registerMessage(nextID(), SyncTempFlagsS2C.class,
                SyncTempFlagsS2C::encode,
                SyncTempFlagsS2C::new,
                SyncTempFlagsS2C::handle);
        CHANNEL.registerMessage(nextID(), PlaceResultS2C.class,
                PlaceResultS2C::encode,
                PlaceResultS2C::new,
                PlaceResultS2C::handle);
    }
}
