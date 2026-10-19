package com.workshopcraft.morefood.blocks;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

import java.util.function.Supplier;

public class BlockCropMoreFood extends CropBlock {
    private final Supplier<Item> seed;

    public BlockCropMoreFood(Properties properties, Supplier<Item> seed) {
        super(properties);
        this.seed = seed;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }
}
