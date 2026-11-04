package com.workshopcraft.morefood.neoforge;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootPool;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

@Mod(MoreFood.MODID)
public class MoreFoodNeoForge {
    public MoreFoodNeoForge(IEventBus modBus) {
        modBus.addListener(MoreFoodNeoForge::onRegister);
        modBus.addListener(MoreFoodNeoForge::onCapabilities);
        NeoForge.EVENT_BUS.addListener(MoreFoodNeoForge::onLootTableLoad);
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent e) -> e.sendRecipes(MoreFoodKitchen.BAKING, MoreFoodKitchen.FRYING,
                MoreFoodKitchen.BOILING, MoreFoodKitchen.ASSEMBLY));
    }

    private static void onRegister(RegisterEvent e) {
        e.register(Registries.DATA_COMPONENT_TYPE, h -> MoreFoodKitchen.registerComponents(h::register));
        e.register(Registries.BLOCK, h -> MoreFoodBlocks.register(h::register));
        e.register(Registries.ITEM, h -> MoreFoodItems.register(h::register));
        e.register(Registries.CREATIVE_MODE_TAB, h -> MoreFoodItems.registerTabs(h::register));
        e.register(Registries.BLOCK_ENTITY_TYPE, h -> MoreFoodKitchen.registerBlockEntities(h::register));
        e.register(Registries.MENU, h -> MoreFoodKitchen.registerMenus(h::register));
        e.register(Registries.RECIPE_TYPE, h -> MoreFoodKitchen.registerRecipeTypes(h::register));
        e.register(Registries.RECIPE_SERIALIZER, h -> MoreFoodKitchen.registerRecipeSerializers(h::register));
        e.register(Registries.RECIPE_BOOK_CATEGORY, h -> MoreFoodKitchen.registerBookCategories(h::register));
        e.register(Registries.PARTICLE_TYPE, h -> MoreFoodKitchen.registerParticles(h::register));
        e.register(Registries.SOUND_EVENT, h -> MoreFoodKitchen.registerSounds(h::register));
    }

    private static void onCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.Item.BLOCK, MoreFoodKitchen.stoveEntity, WorldlyContainerWrapper::new);
        e.registerBlockEntity(Capabilities.Fluid.BLOCK, MoreFoodKitchen.stoveEntity, PotFluidHandler::of);
        e.registerBlockEntity(Capabilities.Item.BLOCK, MoreFoodKitchen.cookingPotEntity, WorldlyContainerWrapper::new);
        e.registerBlockEntity(Capabilities.Fluid.BLOCK, MoreFoodKitchen.cookingPotEntity, PotFluidHandler::ofCampfire);
        e.registerBlockEntity(Capabilities.Item.BLOCK, MoreFoodKitchen.boardEntity, BoardItemHandler::of);
    }

    private static void onLootTableLoad(LootTableLoadEvent e) {
        final LootPool.Builder pool = MoreFood.extraLoot(e.getKey());
        if (pool != null) {
            e.getTable().addPool(pool.build());
        }
    }
}
