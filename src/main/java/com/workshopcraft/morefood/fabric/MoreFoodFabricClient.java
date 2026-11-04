package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.client.AssemblyBoardRenderer;
import com.workshopcraft.morefood.client.BoardLayers;
import com.workshopcraft.morefood.client.CookingPotRenderer;
import com.workshopcraft.morefood.client.SteamParticle;
import com.workshopcraft.morefood.client.StoveRenderer;
import com.workshopcraft.morefood.client.StoveScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
//? if >=26.1 {
import com.workshopcraft.morefood.client.ClientRecipes;
import com.workshopcraft.morefood.client.HasTopping;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
//?} else {
/*import com.workshopcraft.morefood.client.LegacyItemProps;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
*///?}

public class MoreFoodFabricClient implements ClientModInitializer {
    private static final Identifier EARLY = MoreFood.id("early");

    @Override
    public void onInitializeClient() {
        MenuScreens.register(MoreFoodKitchen.STOVE_MENU, StoveScreen::new);
        BlockEntityRendererRegistry.register(MoreFoodKitchen.stoveEntity, StoveRenderer::new);
        BlockEntityRendererRegistry.register(MoreFoodKitchen.cookingPotEntity, CookingPotRenderer::new);
        BlockEntityRendererRegistry.register(MoreFoodKitchen.boardEntity, AssemblyBoardRenderer::new);
        ParticleProviderRegistry.getInstance().register(MoreFoodKitchen.STEAM, SteamParticle.Provider::new);
        //? if >=26.1 {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(BoardLayers.ID, new BoardLayers());
        ConditionalItemModelProperties.ID_MAPPER.put(HasTopping.ID, HasTopping.MAP_CODEC);
        // before recipe viewers read them
        ClientRecipeSynchronizedEvent.EVENT.addPhaseOrdering(EARLY, Event.DEFAULT_PHASE);
        ClientRecipeSynchronizedEvent.EVENT.register(EARLY, (client, recipes) -> ClientRecipes.set(recipes.recipes()));
        //?} else {
        /*ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new Layers());
        LegacyItemProps.register(FabricModelPredicateProviderRegistry::register);
        *///?}
    }

    //? if <26.1 {
    /*private static final class Layers implements IdentifiableResourceReloadListener {
        private final BoardLayers layers = new BoardLayers();

        @Override
        public Identifier getFabricId() {
            return BoardLayers.ID;
        }

        @Override
        public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepare,
                                              ProfilerFiller apply, Executor background, Executor game) {
            return layers.reload(barrier, manager, prepare, apply, background, game);
        }
    }
    *///?}
}
