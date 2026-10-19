package com.workshopcraft.morefood.neoforge;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.client.StoveScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = MoreFood.MODID, dist = Dist.CLIENT)
public class MoreFoodNeoForgeClient {
    public MoreFoodNeoForgeClient(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent e) -> e.register(MoreFoodKitchen.STOVE_MENU, StoveScreen::new));
    }
}
