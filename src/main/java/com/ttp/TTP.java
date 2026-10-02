package com.ttp;

import com.ttp.net.Networking;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TTP.MOD_ID)
public class TTP {

    public static final String MOD_ID = "ttp";
    public static final Logger LOG = LogManager.getLogger();

    public TTP() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Networking::register);
    }
}
