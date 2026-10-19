package com.workshopcraft.morefood.neoforge;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootPool;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MoreFood.MODID)
public class MoreFoodNeoForge {
    public MoreFoodNeoForge(IEventBus modBus) {
        modBus.addListener(MoreFoodNeoForge::onRegister);
        NeoForge.EVENT_BUS.addListener(MoreFoodNeoForge::onLootTableLoad);
    }

    private static void onRegister(RegisterEvent e) {
        e.register(Registries.BLOCK, h -> MoreFoodBlocks.register(h::register));
        e.register(Registries.ITEM, h -> MoreFoodItems.register(h::register));
        e.register(Registries.CREATIVE_MODE_TAB, h -> MoreFoodItems.registerTabs(h::register));
    }

    private static void onLootTableLoad(LootTableLoadEvent e) {
        final LootPool.Builder pool = MoreFood.extraLoot(e.getKey());
        if (pool != null) {
            e.getTable().addPool(pool.build());
        }
    }
}
