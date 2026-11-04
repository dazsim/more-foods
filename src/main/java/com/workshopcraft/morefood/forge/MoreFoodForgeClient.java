package com.workshopcraft.morefood.forge;

//? if forge {
/*import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.client.AssemblyBoardRenderer;
import com.workshopcraft.morefood.client.BoardLayers;
import com.workshopcraft.morefood.client.CookingPotRenderer;
import com.workshopcraft.morefood.client.LegacyItemProps;
import com.workshopcraft.morefood.client.SteamParticle;
import com.workshopcraft.morefood.client.StoveRenderer;
import com.workshopcraft.morefood.client.StoveScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class MoreFoodForgeClient {
    private MoreFoodForgeClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent e) -> e.enqueueWork(() -> {
            MenuScreens.register(MoreFoodKitchen.STOVE_MENU, StoveScreen::new);
            LegacyItemProps.register(ItemProperties::register);
        }));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> {
            e.registerBlockEntityRenderer(MoreFoodKitchen.stoveEntity, StoveRenderer::new);
            e.registerBlockEntityRenderer(MoreFoodKitchen.cookingPotEntity, CookingPotRenderer::new);
            e.registerBlockEntityRenderer(MoreFoodKitchen.boardEntity, AssemblyBoardRenderer::new);
        });
        modBus.addListener((RegisterClientReloadListenersEvent e) -> e.registerReloadListener(new BoardLayers()));
        modBus.addListener((RegisterParticleProvidersEvent e) -> e.registerSpriteSet(MoreFoodKitchen.STEAM, SteamParticle.Provider::new));
    }
}
*///?}
