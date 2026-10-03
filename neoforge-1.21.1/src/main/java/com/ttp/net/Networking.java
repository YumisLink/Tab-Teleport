package com.ttp.net;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class Networking {
    private Networking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.1");
        registrar.playToServer(TeleportRequestPack.TYPE, TeleportRequestPack.STREAM_CODEC,
                (payload, context) -> payload.handle(context));
        registrar.playToServer(PlaceMarkerC2S.TYPE, PlaceMarkerC2S.STREAM_CODEC,
                (payload, context) -> payload.handle(context));
        registrar.playToServer(RenameQueueC2S.TYPE, RenameQueueC2S.STREAM_CODEC,
                (payload, context) -> payload.handle(context));
        registrar.playToClient(SyncOwnQueueS2C.TYPE, SyncOwnQueueS2C.STREAM_CODEC,
                (payload, context) -> payload.handle(context));
        registrar.playToClient(SyncTempFlagsS2C.TYPE, SyncTempFlagsS2C.STREAM_CODEC,
                (payload, context) -> payload.handle(context));
        registrar.playToClient(PlaceResultS2C.TYPE, PlaceResultS2C.STREAM_CODEC,
                (payload, context) -> payload.handle(context));
    }
}
