package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/^* Stands in for the 26.1 value output, writing straight into a compound tag. ^/
public final class ValueOutput {
    private final CompoundTag tag;
    private final HolderLookup.@Nullable Provider registries;

    public ValueOutput(CompoundTag tag, HolderLookup.@Nullable Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    public void putInt(String key, int value) {
        tag.putInt(key, value);
    }

    public void putLong(String key, long value) {
        tag.putLong(key, value);
    }

    public void putBoolean(String key, boolean value) {
        tag.putBoolean(key, value);
    }

    public <T> void store(String key, Codec<T> codec, T value) {
        codec.encodeStart(ValueInput.ops(registries), value).result().ifPresent(t -> tag.put(key, t));
    }

    public ValueOutput child(String key) {
        final CompoundTag child = new CompoundTag();
        tag.put(key, child);
        return new ValueOutput(child, registries);
    }

    public void saveAllItems(NonNullList<ItemStack> items) {
        //? if <1.21 {
        /^ContainerHelper.saveAllItems(tag, items);
        ^///?} else {
        ContainerHelper.saveAllItems(tag, items, registries);
        //?}
    }
}
*///?}
