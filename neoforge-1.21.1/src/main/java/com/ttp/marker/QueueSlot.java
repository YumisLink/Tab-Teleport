package com.ttp.marker;

import net.minecraft.nbt.CompoundTag;

public class QueueSlot {

    public static final int SLOT_COUNT = 3;
    public static final int MAX_NAME_LEN = 32;

    private boolean filled;
    private String name = "";
    private Marker marker;

    public boolean isFilled() {
        return filled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.substring(0, Math.min(name.length(), MAX_NAME_LEN));
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        this.filled = marker != null;
    }

    public void clear() {
        this.filled = false;
        this.marker = null;
        this.name = "";
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("filled", this.filled);
        tag.putString("name", this.name);
        if (this.filled && this.marker != null) {
            tag.put("marker", this.marker.save());
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        this.filled = tag.getBoolean("filled");
        this.name = tag.getString("name");
        if (this.filled && tag.contains("marker")) {
            this.marker = Marker.load(tag.getCompound("marker"));
            if (this.marker == null) {
                this.filled = false;
            }
        } else {
            this.marker = null;
        }
    }

    public QueueSlot copy() {
        QueueSlot copy = new QueueSlot();
        copy.filled = this.filled;
        copy.name = this.name;
        copy.marker = this.marker;
        return copy;
    }
}
