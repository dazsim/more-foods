package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.client.SteamParticle;
import com.workshopcraft.morefood.client.StoveRenderer;
import com.workshopcraft.morefood.client.StoveScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public class MoreFoodFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(MoreFoodKitchen.STOVE_MENU, StoveScreen::new);
        BlockEntityRendererRegistry.register(MoreFoodKitchen.stoveEntity, StoveRenderer::new);
        ParticleProviderRegistry.getInstance().register(MoreFoodKitchen.STEAM, SteamParticle.Provider::new);
    }
}
