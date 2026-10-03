package com.ttp.screen;

import com.ttp.client.ClientMarkerData;
import com.ttp.marker.Marker;
import com.ttp.marker.QueueSlot;
import com.ttp.net.RenameQueueC2S;
import com.ttp.net.TeleportRequestPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PlayerListScreen extends Screen {

    private static final int PANEL_W = 400;
    private static final int VISIBLE_ROWS = 5;
    private static final int COLUMNS = 2;
    private static final int VISIBLE_PLAYERS = VISIBLE_ROWS * COLUMNS;
    private static final int ROW_HEIGHT = 22;
    private static final int HEADER_HEIGHT = 52;
    private static final int BTN_W = 54;
    private static final int BTN_H = 18;
    private static final int FOOTER_PAD = 8;

    private final List<PlayerInfo> players = new ArrayList<>();
    private int scrollOffset = 0;
    private int editingSlot = -1;
    private EditBox nameEditBox;

    public PlayerListScreen() {
        super(Component.translatable("ttp.screen.title"));
    }

    @Override
    protected void init() {
        refreshPlayers();
        refreshWidgets();
    }

    private void refreshPlayers() {
        Minecraft mc = Minecraft.getInstance();
        this.players.clear();
        if (mc.getConnection() == null || mc.player == null) {
            return;
        }
        UUID self = mc.player.getUUID();
        for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
            if (!info.getProfile().getId().equals(self)) {
                this.players.add(info);
            }
        }
        this.players.sort(Comparator.comparing(p -> p.getProfile().getName(), String.CASE_INSENSITIVE_ORDER));
        int maxScroll = Math.max(0, this.players.size() - VISIBLE_PLAYERS);
        if (this.scrollOffset > maxScroll) {
            this.scrollOffset = maxScroll;
        }
    }

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int panelY() {
        int panelH = panelHeight();
        return (this.height - panelH) / 2;
    }

    private int panelHeight() {
        return HEADER_HEIGHT + VISIBLE_ROWS * ROW_HEIGHT + FOOTER_PAD;
    }

    private void refreshWidgets() {
        if (this.editingSlot >= 0 && this.nameEditBox != null) {
            return;
        }
        this.clearWidgets();
        int px = panelX();
        int py = panelY();
        int cellW = PANEL_W / QueueSlot.SLOT_COUNT;

        QueueSlot[] queue = ClientMarkerData.getOwnQueue();
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            int cellX = px + i * cellW;
            int slot = i;
            QueueSlot qs = queue[i];

            int btnX = cellX + cellW - BTN_W - 4;
            Button tpBtn = Button.builder(Component.translatable("ttp.screen.header_tp"), btn -> {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new TeleportRequestPack(TeleportRequestPack.Kind.OWN_QUEUE, "", slot));
                this.onClose();
            }).bounds(btnX, py + 4, BTN_W, BTN_H).build();
            tpBtn.active = qs.isFilled();
            this.addRenderableWidget(tpBtn);
        }

        int bodyY = py + HEADER_HEIGHT;
        int colW = PANEL_W / COLUMNS;

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = this.scrollOffset + row * COLUMNS + col;
                if (index >= this.players.size()) {
                    continue;
                }
                PlayerInfo pinfo = this.players.get(index);
                int rowY = bodyY + row * ROW_HEIGHT;
                int colX = px + col * colW;

                String name = pinfo.getProfile().getName();
                int nameMaxW = colW - BTN_W * 2 - 12;
                if (this.font.width(name) > nameMaxW) {
                    name = this.font.plainSubstrByWidth(name, nameMaxW - 6) + "...";
                }

                int markX = colX + colW - BTN_W - 4;
                int tpX = markX - BTN_W - 2;

                Button tpPlayer = Button.builder(Component.translatable("ttp.screen.teleport"), btn -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new TeleportRequestPack(
                            TeleportRequestPack.Kind.PLAYER, pinfo.getProfile().getId().toString(), -1));
                    this.onClose();
                }).bounds(tpX, rowY, BTN_W, BTN_H).build();
                this.addRenderableWidget(tpPlayer);

                UUID pid = pinfo.getProfile().getId();
                boolean hasTemp = ClientMarkerData.hasTemp(pid);
                Button marked = Button.builder(Component.translatable("ttp.screen.marked"), btn -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new TeleportRequestPack(
                            TeleportRequestPack.Kind.TEMP, pid.toString(), -1));
                    this.onClose();
                }).bounds(markX, rowY, BTN_W, BTN_H).build();
                marked.active = hasTemp;
                this.addRenderableWidget(marked);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        int px = panelX();
        int py = panelY();
        int ph = panelHeight();

        graphics.fill(px, py, px + PANEL_W, py + ph, 0xCC000000);
        graphics.fill(px + 1, py + 1, px + PANEL_W - 1, py + ph - 1, 0xFF1A1A2E);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, py + 4, 0xFFFFFF);

        QueueSlot[] queue = ClientMarkerData.getOwnQueue();
        int cellW = PANEL_W / QueueSlot.SLOT_COUNT;
        for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
            int cellX = px + i * cellW + 4;
            QueueSlot qs = queue[i];
            String displayName = qs.getName();
            boolean placeholder = displayName == null || displayName.isBlank();
            if (placeholder) {
                displayName = Component.translatable("ttp.marker.default_name", i + 1).getString();
            }
            int nameColor = placeholder ? 0x888888 : 0xFFFFFF;
            graphics.drawString(this.font, displayName, cellX, py + 26, nameColor);

            if (qs.isFilled() && qs.getMarker() != null) {
                Marker m = qs.getMarker();
                String coords = shortDim(m.dimension().location().toString()) + " " + m.coordString();
                graphics.drawString(this.font, coords, cellX, py + 38, 0xAAAAAA);
            }
        }

        int bodyY = py + HEADER_HEIGHT;
        int colW = PANEL_W / COLUMNS;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = this.scrollOffset + row * COLUMNS + col;
                if (index >= this.players.size()) {
                    continue;
                }
                PlayerInfo pinfo = this.players.get(index);
                int rowY = bodyY + row * ROW_HEIGHT;
                int colX = px + col * colW + 4;
                String name = pinfo.getProfile().getName();
                int nameMaxW = colW - BTN_W * 2 - 12;
                if (this.font.width(name) > nameMaxW) {
                    name = this.font.plainSubstrByWidth(name, nameMaxW - 6) + "...";
                }
                graphics.drawString(this.font, name, colX, rowY + 5, 0x00FF7F);
            }
        }

        if (this.players.isEmpty()) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("ttp.screen.no_players"),
                    this.width / 2, bodyY + 40, 0x888888);
        }

        if (this.nameEditBox != null) {
            this.nameEditBox.render(graphics, mouseX, mouseY, partialTick);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.nameEditBox == null) {
            int px = panelX();
            int py = panelY();
            int cellW = PANEL_W / QueueSlot.SLOT_COUNT;
            for (int i = 0; i < QueueSlot.SLOT_COUNT; i++) {
                int cellX = px + i * cellW;
                if (mouseX >= cellX && mouseX < cellX + cellW - BTN_W - 6
                        && mouseY >= py + 22 && mouseY < py + 36) {
                    QueueSlot qs = ClientMarkerData.getOwnQueue()[i];
                    if (qs.isFilled()) {
                        startRename(i, cellX + 4, py + 22, cellW - BTN_W - 10);
                        return true;
                    }
                }
            }
        }
        if (this.nameEditBox != null && !this.nameEditBox.isFocused()) {
            finishRename();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void startRename(int slot, int x, int y, int w) {
        this.editingSlot = slot;
        QueueSlot qs = ClientMarkerData.getOwnQueue()[slot];
        this.clearWidgets();
        this.nameEditBox = new EditBox(this.font, x, y, w, 14, Component.empty());
        this.nameEditBox.setValue(qs.getName());
        this.nameEditBox.setMaxLength(QueueSlot.MAX_NAME_LEN);
        this.nameEditBox.setFocused(true);
        this.addRenderableWidget(this.nameEditBox);
    }

    private void finishRename() {
        if (this.editingSlot < 0 || this.nameEditBox == null) {
            return;
        }
        String newName = this.nameEditBox.getValue();
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new RenameQueueC2S(this.editingSlot, newName));
        this.nameEditBox = null;
        this.editingSlot = -1;
        this.refreshWidgets();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameEditBox != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                finishRename();
                return true;
            }
            if (this.nameEditBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalDelta, double delta) {
        int maxScroll = Math.max(0, this.players.size() - VISIBLE_PLAYERS);
        if (delta > 0 && this.scrollOffset > 0) {
            this.scrollOffset--;
            refreshWidgets();
            return true;
        }
        if (delta < 0 && this.scrollOffset < maxScroll) {
            this.scrollOffset++;
            refreshWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalDelta, delta);
    }

    private static String shortDim(String dim) {
        int colon = dim.indexOf(':');
        return colon >= 0 ? dim.substring(colon + 1) : dim;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
