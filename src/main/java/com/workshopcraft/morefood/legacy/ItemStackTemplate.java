package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/^* Stands in for the 26.1 item stack template on older versions. ^/
public final class ItemStackTemplate {
    //? if <1.21 {
    /^private static final Codec<CompoundTag> NBT = Codec.either(CompoundTag.CODEC, Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(TagParser.parseTag(s));
        } catch (CommandSyntaxException e) {
            return DataResult.error(e::getMessage);
        }
    }, CompoundTag::toString)).xmap(e -> e.map(t -> t, t -> t), com.mojang.datafixers.util.Either::left);

    public static final Codec<ItemStackTemplate> CODEC = RecordCodecBuilder.create(i -> i.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(o -> o.stack.getItem()),
            Codec.INT.optionalFieldOf("count", 1).forGetter(o -> o.stack.getCount()),
            NBT.optionalFieldOf("nbt").forGetter(o -> Optional.ofNullable(o.stack.getTag()))
    ).apply(i, (item, count, tag) -> {
        final ItemStack stack = new ItemStack(item, count);
        tag.ifPresent(t -> stack.setTag(t.copy()));
        return new ItemStackTemplate(stack);
    }));
    ^///?} else {
    public static final Codec<ItemStackTemplate> CODEC = ItemStack.CODEC.xmap(ItemStackTemplate::new, o -> o.stack);
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ItemStackTemplate> STREAM_CODEC =
            ItemStack.STREAM_CODEC.map(ItemStackTemplate::new, o -> o.stack);
    //?}

    private final ItemStack stack;

    private ItemStackTemplate(ItemStack stack) {
        this.stack = stack.copy();
    }

    public ItemStackTemplate(Item item) {
        this(new ItemStack(item));
    }

    public ItemStackTemplate(Item item, int count) {
        this(new ItemStack(item, count));
    }

    public static ItemStackTemplate fromNonEmptyStack(ItemStack stack) {
        return new ItemStackTemplate(stack);
    }

    public Item item() {
        return stack.getItem();
    }

    public int count() {
        return stack.getCount();
    }

    public ItemStack create() {
        return stack.copy();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ItemStackTemplate t && ItemStack.matches(stack, t.stack);
    }

    @Override
    public int hashCode() {
        return stack.getItem().hashCode() * 31 + stack.getCount();
    }
}
*///?}
