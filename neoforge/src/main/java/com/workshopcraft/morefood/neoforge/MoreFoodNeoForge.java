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
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

@Mod(MoreFood.MODID)
public class MoreFoodNeoForge {
    public MoreFoodNeoForge(IEventBus modBus) {
        modBus.addListener(MoreFoodNeoForge::onRegister);
        modBus.addListener(MoreFoodNeoForge::onCapabilities);
        NeoForge.EVENT_BUS.addListener(MoreFoodNeoForge::onLootTableLoad);
    }

    private static void onRegister(RegisterEvent e) {
        e.register(Registries.BLOCK, h -> MoreFoodBlocks.register(h::register));
        e.register(Registries.ITEM, h -> MoreFoodItems.register(h::register));
        e.register(Registries.CREATIVE_MODE_TAB, h -> MoreFoodItems.registerTabs(h::register));
        e.register(Registries.BLOCK_ENTITY_TYPE, h -> MoreFoodKitchen.registerBlockEntities(h::register));
        e.register(Registries.MENU, h -> MoreFoodKitchen.registerMenus(h::register));
        e.register(Registries.RECIPE_TYPE, h -> MoreFoodKitchen.registerRecipeTypes(h::register));
        e.register(Registries.RECIPE_SERIALIZER, h -> MoreFoodKitchen.registerRecipeSerializers(h::register));
        e.register(Registries.RECIPE_BOOK_CATEGORY, h -> MoreFoodKitchen.registerBookCategories(h::register));
    }

    private static void onCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.Item.BLOCK, MoreFoodKitchen.stoveEntity, WorldlyContainerWrapper::new);
    }

    private static void onLootTableLoad(LootTableLoadEvent e) {
        final LootPool.Builder pool = MoreFood.extraLoot(e.getKey());
        if (pool != null) {
            e.getTable().addPool(pool.build());
        }
    }
}
