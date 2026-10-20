package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.client.AssemblyBoardRenderer;
import com.workshopcraft.morefood.client.BoardLayers;
import com.workshopcraft.morefood.client.CookingPotRenderer;
import com.workshopcraft.morefood.client.HasTopping;
import com.workshopcraft.morefood.client.SteamParticle;
import com.workshopcraft.morefood.client.StoveRenderer;
import com.workshopcraft.morefood.client.StoveScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.server.packs.PackType;

public class MoreFoodFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(MoreFoodKitchen.STOVE_MENU, StoveScreen::new);
        BlockEntityRendererRegistry.register(MoreFoodKitchen.stoveEntity, StoveRenderer::new);
        BlockEntityRendererRegistry.register(MoreFoodKitchen.cookingPotEntity, CookingPotRenderer::new);
        BlockEntityRendererRegistry.register(MoreFoodKitchen.boardEntity, AssemblyBoardRenderer::new);
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(BoardLayers.ID, new BoardLayers());
        ConditionalItemModelProperties.ID_MAPPER.put(HasTopping.ID, HasTopping.MAP_CODEC);
        ParticleProviderRegistry.getInstance().register(MoreFoodKitchen.STEAM, SteamParticle.Provider::new);
    }
}
