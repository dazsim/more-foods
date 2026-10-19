package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.client.StoveScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class MoreFoodFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(MoreFoodKitchen.STOVE_MENU, StoveScreen::new);
    }
}
