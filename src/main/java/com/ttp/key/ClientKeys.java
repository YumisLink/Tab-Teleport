package com.ttp.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.ttp.TTP;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = TTP.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientKeys {

    public static final String CATEGORY = "key.ttp.category";
    public static KeyMapping MARK;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        MARK = new KeyMapping(
                "key.ttp.mark",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY);
        event.register(MARK);
    }
}
