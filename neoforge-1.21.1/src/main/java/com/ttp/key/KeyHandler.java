package com.ttp.key;

import com.ttp.client.MarkerPlacement;
import com.ttp.screen.PlayerListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = com.ttp.TTP.MOD_ID, value = Dist.CLIENT)
public class KeyHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        if (mc.options.keyPlayerList.isDown()) {
            if (mc.screen == null) {
                mc.setScreen(new PlayerListScreen());
            }
        }

        if (ClientKeys.MARK == null) {
            return;
        }
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        if (event.getKey() != ClientKeys.MARK.getKey().getValue()) {
            return;
        }
        boolean queue = hasControlDown();
        MarkerPlacement.tryPlace(queue);
    }

    private static boolean hasControlDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
}
