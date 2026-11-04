package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/^* Stands in for the 26.1 value input, reading straight from a compound tag. ^/
public final class ValueInput {
    private final CompoundTag tag;
    private final HolderLookup.@Nullable Provider registries;

    public ValueInput(CompoundTag tag, HolderLookup.@Nullable Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    static DynamicOps<Tag> ops(HolderLookup.@Nullable Provider registries) {
        //? if <1.21 {
        /^return NbtOps.INSTANCE;
        ^///?} else {
        return registries != null ? registries.createSerializationContext(NbtOps.INSTANCE) : NbtOps.INSTANCE;
        //?}
    }

    public CompoundTag tag() {
        return tag;
    }

    public int getIntOr(String key, int fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
    }

    public long getLongOr(String key, long fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : fallback;
    }

    public boolean getBooleanOr(String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
    }

    public <T> Optional<T> read(String key, Codec<T> codec) {
        final Tag value = tag.get(key);
        if (value == null) return Optional.empty();
        return codec.parse(ops(registries), value).result();
    }

    public Optional<ValueInput> child(String key) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? Optional.of(new ValueInput(tag.getCompound(key), registries)) : Optional.empty();
    }

    public ValueInput childOrEmpty(String key) {
        return new ValueInput(tag.getCompound(key), registries);
    }

    public void loadAllItems(NonNullList<ItemStack> items) {
        //? if <1.21 {
        /^ContainerHelper.loadAllItems(tag, items);
        ^///?} else {
        ContainerHelper.loadAllItems(tag, items, registries);
        //?}
    }
}
*///?}
