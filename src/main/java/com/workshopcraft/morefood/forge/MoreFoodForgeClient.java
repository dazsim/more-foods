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

    private static void cutout() {
        final net.minecraft.client.renderer.RenderType layer = net.minecraft.client.renderer.RenderType.cutout();
        net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.workshopcraft.morefood.MoreFoodBlocks.cropTomato, layer);
        net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.workshopcraft.morefood.MoreFoodBlocks.cropLettuce, layer);
        net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.workshopcraft.morefood.MoreFoodBlocks.cropOnion, layer);
        for (net.minecraft.world.level.block.Block crop : com.workshopcraft.morefood.MoreFoodBlocks.CROPS.values()) {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(crop, layer);
        }
        for (net.minecraft.world.level.block.Block sapling : com.workshopcraft.morefood.MoreFoodBlocks.SAPLINGS.values()) {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(sapling, layer);
        }
        for (net.minecraft.world.level.block.Block pod : com.workshopcraft.morefood.MoreFoodBlocks.PODS.values()) {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(pod, layer);
        }
        for (net.minecraft.world.level.block.Block leaves : com.workshopcraft.morefood.MoreFoodBlocks.LEAVES.values()) {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(leaves, net.minecraft.client.renderer.RenderType.cutoutMipped());
        }
    }

    static void init(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent e) -> e.enqueueWork(() -> {
            MenuScreens.register(MoreFoodKitchen.STOVE_MENU, StoveScreen::new);
            MenuScreens.register(com.workshopcraft.morefood.MoreFoodMachines.GRINDER_MENU, com.workshopcraft.morefood.client.KitchenMachineScreen::new);
            MenuScreens.register(com.workshopcraft.morefood.MoreFoodMachines.PRESS_MENU, com.workshopcraft.morefood.client.KitchenMachineScreen::new);
            LegacyItemProps.register(ItemProperties::register);
            cutout();
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
