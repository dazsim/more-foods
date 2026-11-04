package com.workshopcraft.morefood.forge;

//? if forge {
/*import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.board.AssemblyBoardBlockEntity;
import com.workshopcraft.morefood.stove.CookingPotBlockEntity;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@Mod(MoreFood.MODID)
public class MoreFoodForge {
    private static final Identifier CAPS = MoreFood.id("transfer");

    public MoreFoodForge(FMLJavaModLoadingContext context) {
        final IEventBus modBus = context.getModEventBus();
        modBus.addListener(MoreFoodForge::onRegister);
        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(() -> MoreFoodItems.COMPOST.forEach((name, chance) ->
                ComposterBlock.COMPOSTABLES.put(BuiltInRegistries.ITEM.get(MoreFood.id(name)), (float) chance))));
        MinecraftForge.EVENT_BUS.addListener(MoreFoodForge::onLootTableLoad);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, MoreFoodForge::onAttach);
        if (FMLEnvironment.dist == Dist.CLIENT) MoreFoodForgeClient.init(modBus);
    }

    private static void onRegister(RegisterEvent e) {
        e.register(Registries.BLOCK, h -> MoreFoodBlocks.register(h::register));
        e.register(Registries.ITEM, h -> MoreFoodItems.register(h::register));
        e.register(Registries.CREATIVE_MODE_TAB, h -> MoreFoodItems.registerTabs(h::register));
        e.register(Registries.BLOCK_ENTITY_TYPE, h -> MoreFoodKitchen.registerBlockEntities(h::register));
        e.register(Registries.MENU, h -> MoreFoodKitchen.registerMenus(h::register));
        e.register(Registries.RECIPE_TYPE, h -> MoreFoodKitchen.registerRecipeTypes(h::register));
        e.register(Registries.RECIPE_SERIALIZER, h -> MoreFoodKitchen.registerRecipeSerializers(h::register));
        e.register(Registries.PARTICLE_TYPE, h -> MoreFoodKitchen.registerParticles(h::register));
        e.register(Registries.SOUND_EVENT, h -> MoreFoodKitchen.registerSounds(h::register));
    }

    private static void onLootTableLoad(LootTableLoadEvent e) {
        final LootPool.Builder pool = MoreFood.extraLoot(e.getName());
        if (pool != null) e.getTable().addPool(pool.build());
    }

    // the stove and pot item handlers come from their containers, sided
    private static void onAttach(AttachCapabilitiesEvent<BlockEntity> e) {
        if (e.getObject() instanceof StoveBlockEntity stove) {
            e.addCapability(CAPS, new Provider<>(ForgeCapabilities.FLUID_HANDLER, side -> PotFluidHandler.of(stove, side)));
        } else if (e.getObject() instanceof CookingPotBlockEntity pot) {
            e.addCapability(CAPS, new Provider<>(ForgeCapabilities.FLUID_HANDLER, side -> PotFluidHandler.ofCampfire(pot, side)));
        } else if (e.getObject() instanceof AssemblyBoardBlockEntity board) {
            e.addCapability(CAPS, new Provider<>(ForgeCapabilities.ITEM_HANDLER, side -> BoardItemHandler.of(board, side)));
        }
    }

    private static final class Provider<H> implements ICapabilityProvider {
        private final Capability<H> cap;
        private final Function<@Nullable Direction, @Nullable H> make;
        private final Map<Direction, LazyOptional<H>> sided = new EnumMap<>(Direction.class);
        private @Nullable LazyOptional<H> unsided;

        Provider(Capability<H> cap, Function<@Nullable Direction, @Nullable H> make) {
            this.cap = cap;
            this.make = make;
        }

        private LazyOptional<H> lookup(@Nullable Direction side) {
            final H handler = make.apply(side);
            return handler == null ? LazyOptional.empty() : LazyOptional.of(() -> handler);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> asked, @Nullable Direction side) {
            if (asked != cap) return LazyOptional.empty();
            if (side == null) {
                if (unsided == null) unsided = lookup(null);
                return unsided.cast();
            }
            return sided.computeIfAbsent(side, this::lookup).cast();
        }
    }
}
*///?}
