package com.workshopcraft.morefood.neoforge;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.client.CookingPotRenderer;
import com.workshopcraft.morefood.client.SteamParticle;
import com.workshopcraft.morefood.client.StoveRenderer;
import com.workshopcraft.morefood.client.StoveScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@Mod(value = MoreFood.MODID, dist = Dist.CLIENT)
public class MoreFoodNeoForgeClient {
    public MoreFoodNeoForgeClient(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent e) -> e.register(MoreFoodKitchen.STOVE_MENU, StoveScreen::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> {
            e.registerBlockEntityRenderer(MoreFoodKitchen.stoveEntity, StoveRenderer::new);
            e.registerBlockEntityRenderer(MoreFoodKitchen.cookingPotEntity, CookingPotRenderer::new);
        });
        modBus.addListener((RegisterParticleProvidersEvent e) -> e.registerSpriteSet(MoreFoodKitchen.STEAM, SteamParticle.Provider::new));
    }
}
