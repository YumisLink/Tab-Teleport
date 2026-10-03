package com.ttp.marker;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record Marker(ResourceKey<Level> dimension, BlockPos pos) {

    private static final String TAG_DIM = "dim";
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_DIM, this.dimension.location().toString());
        tag.putInt(TAG_X, this.pos.getX());
        tag.putInt(TAG_Y, this.pos.getY());
        tag.putInt(TAG_Z, this.pos.getZ());
        return tag;
    }

    public static Marker load(CompoundTag tag) {
        ResourceLocation dimId = ResourceLocation.tryParse(tag.getString(TAG_DIM));
        if (dimId == null) {
            return null;
        }
        ResourceKey<Level> dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimId);
        BlockPos pos = new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
        return new Marker(dim, pos);
    }

    public String coordString() {
        return this.pos.getX() + " " + this.pos.getY() + " " + this.pos.getZ();
    }
}
