package com.ttp.client;

import com.ttp.net.PlaceMarkerC2S;
import com.ttp.screen.QueueReplaceScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MarkerPlacement {

    private MarkerPlacement() {
    }

    public static void tryPlace(boolean queue) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.screen != null) {
            return;
        }
        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            mc.player.displayClientMessage(
                    Component.translatable("ttp.msg.mark_fail").withStyle(ChatFormatting.RED), true);
            return;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos aimed = blockHit.getBlockPos();
        Direction face = blockHit.getDirection();

        if (queue && ClientMarkerData.isQueueFull()) {
            mc.setScreen(new QueueReplaceScreen(aimed, face));
            return;
        }

        int replaceSlot = -1;
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new PlaceMarkerC2S(
                aimed.getX(), aimed.getY(), aimed.getZ(), face, queue, replaceSlot));
    }
}
