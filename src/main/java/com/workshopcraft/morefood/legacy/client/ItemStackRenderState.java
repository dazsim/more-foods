package com.workshopcraft.morefood.legacy.client;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/^* Stands in for the 26.1 item render state by keeping the stack and drawing it later. ^/
public class ItemStackRenderState {
    ItemStack stack = ItemStack.EMPTY;
    ItemDisplayContext context = ItemDisplayContext.FIXED;
    @Nullable Level level;
    int seed;

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, int overlay, int outline) {
        if (stack.isEmpty()) return;
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, light, overlay, pose, collector.buffers(), level, seed);
    }
}
*///?}
