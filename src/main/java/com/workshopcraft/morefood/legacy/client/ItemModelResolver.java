package com.workshopcraft.morefood.legacy.client;

//? if <26.1 {
/*import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ItemModelResolver {
    private static final ItemModelResolver INSTANCE = new ItemModelResolver();

    public static ItemModelResolver of(BlockEntityRendererProvider.Context context) {
        return INSTANCE;
    }

    public void updateForTopItem(ItemStackRenderState state, ItemStack stack, ItemDisplayContext context, @Nullable Level level,
                                 @Nullable LivingEntity entity, int seed) {
        state.stack = stack.copy();
        state.context = context;
        state.level = level;
        state.seed = seed;
    }
}
*///?}
