package com.workshopcraft.morefood.worldgen;

import com.workshopcraft.morefood.MoreFood;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.BiConsumer;

public class MoreFoodWorldGen {
    public static final ResourceKey<PlacedFeature> JACKFRUIT = placed("tree_jackfruit");
    public static final ResourceKey<PlacedFeature> COCONUT = placed("tree_coconut");
    public static final ResourceKey<PlacedFeature> OLIVE = placed("tree_olive");
    public static final ResourceKey<PlacedFeature> AVOCADO = placed("tree_avocado");

    //? if >=26.3 {
    /*public static void register(BiConsumer<Identifier, com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.levelgen.feature.Feature>> registry) {
        for (FruitTreeFeature.Type type : FruitTreeFeature.Type.values()) {
            registry.accept(MoreFood.id(type.name().toLowerCase() + "_tree"), FruitTreeFeature.codecOf(type));
        }
    }
    *///?} else {
    public static void register(BiConsumer<Identifier, net.minecraft.world.level.levelgen.feature.Feature<?>> registry) {
        for (FruitTreeFeature.Type type : FruitTreeFeature.Type.values()) {
            registry.accept(MoreFood.id(type.name().toLowerCase() + "_tree"), new FruitTreeFeature(type));
        }
    }
    //?}

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, MoreFood.id(name));
    }
}
