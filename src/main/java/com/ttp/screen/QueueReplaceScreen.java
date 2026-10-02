package com.ttp.screen;

import com.ttp.marker.Marker;
import com.ttp.marker.QueueSlot;
import com.ttp.client.ClientMarkerData;
import com.ttp.net.Networking;
import com.ttp.net.PlaceMarkerC2S;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class QueueReplaceScreen extends Screen {

    private final BlockPos aimedBlock;
    private final Direction face;

    public QueueReplaceScreen(BlockPos aimedBlock, Direction face) {
        super(Component.translatable("ttp.screen.replace_picker"));
        this.aimedBlock = aimedBlock;
        this.face = face;
    }

    @Override
    protected void init() {
        int panelW = 280;
        int panelH = 30 + QueueSlot.SLOT_COUNT * 28 + 16;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        int rowY = panelY + 28;

        QueueSlot[] slots = ClientMarkerData.getOwnQueue();
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            int slot = i;
            QueueSlot qs = slots[i];
            String label = buildRowLabel(i, qs);
            int y = rowY + i * 28;
            this.addRenderableWidget(Button.builder(Component.literal(label), btn -> {
                Networking.CHANNEL.sendToServer(new PlaceMarkerC2S(
                        this.aimedBlock.getX(), this.aimedBlock.getY(), this.aimedBlock.getZ(),
                        this.face, true, slot));
                this.onClose();
            }).bounds(panelX + 8, y, panelW - 16, 22).build());
        }
    }

    private String buildRowLabel(int index, QueueSlot qs) {
        String name = qs.getName();
        if (name == null || name.isBlank()) {
            name = Component.translatable("ttp.marker.default_name", index + 1).getString();
        }
        if (!qs.isFilled() || qs.getMarker() == null) {
            return name + " —";
        }
        Marker m = qs.getMarker();
        return name + " | " + shortDim(m.dimension().location().toString()) + " " + m.coordString();
    }

    private static String shortDim(String dim) {
        int colon = dim.indexOf(':');
        return colon >= 0 ? dim.substring(colon + 1) : dim;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int panelW = 280;
        int panelH = 30 + QueueSlot.SLOT_COUNT * 28 + 16;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC000000);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 8, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
