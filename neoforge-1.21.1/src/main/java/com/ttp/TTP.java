package com.ttp;

import com.ttp.net.Networking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TTP.MOD_ID)
public class TTP {
    public static final String MOD_ID = "ttp";
    public static final Logger LOG = LogManager.getLogger();

    public TTP(IEventBus modEventBus) {
        modEventBus.addListener(Networking::register);
    }
}
