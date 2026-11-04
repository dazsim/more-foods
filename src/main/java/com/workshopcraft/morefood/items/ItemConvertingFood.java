package com.workshopcraft.morefood.items;

//? if <1.20.5 {
/*import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/^* Food that hands back its bowl or bottle, still stacking to 64 as on 26.1. ^/
public class ItemConvertingFood extends Item {
    private final Item converts;

    public ItemConvertingFood(Properties properties, Item converts) {
        super(properties);
        this.converts = converts;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        final ItemStack left = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && player.getAbilities().instabuild) return left;
        final ItemStack vessel = new ItemStack(converts);
        if (left.isEmpty()) return vessel;
        if (entity instanceof Player player && !player.getInventory().add(vessel)) player.drop(vessel, false);
        return left;
    }
}
*///?}
