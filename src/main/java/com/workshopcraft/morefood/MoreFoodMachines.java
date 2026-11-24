package com.workshopcraft.morefood;

import com.workshopcraft.morefood.machine.KitchenMachineBlockEntity;
import com.workshopcraft.morefood.machine.KitchenMachineItem;
import com.workshopcraft.morefood.machine.KitchenMachineMenu;
import com.workshopcraft.morefood.machine.KitchenSinkBlock;
import com.workshopcraft.morefood.machine.KitchenSinkBlockEntity;
import com.workshopcraft.morefood.machine.MilkChurnBlock;
import com.workshopcraft.morefood.machine.MilkChurnBlockEntity;
import com.workshopcraft.workshopcore.compat.Registration;
import com.workshopcraft.workshopcore.machine.Assembly;
import com.workshopcraft.workshopcore.machine.ChassisBlock;
import com.workshopcraft.workshopcore.machine.ChassisBlockEntity;
import com.workshopcraft.workshopcore.machine.ChassisTier;
import com.workshopcraft.workshopcore.machine.Drives;
import com.workshopcraft.workshopcore.machine.Finishes;
import com.workshopcraft.workshopcore.machine.MachineBlock;
import com.workshopcraft.workshopcore.machine.MachineCore;
import com.workshopcraft.workshopcore.machine.FluidTanks;
import com.workshopcraft.workshopcore.machine.Machines;
import com.workshopcraft.workshopcore.machine.PowerPart;
import com.workshopcraft.workshopcore.machine.PowerParts;
import com.workshopcraft.workshopcore.machine.Voltage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/** The kitchen machines built from Workshop Core parts, the sink, and the vegan foods they make. */
public final class MoreFoodMachines {
    public static final ChassisTier KITCHEN = new ChassisTier("kitchen", 1.0, 1.0);
    public static final String FINISH = "kitchen";
    public static final MenuType<KitchenMachineMenu> GRINDER_MENU = new MenuType<>(KitchenMachineMenu::grinder, FeatureFlags.VANILLA_SET);
    public static final MenuType<KitchenMachineMenu> PRESS_MENU = new MenuType<>(KitchenMachineMenu::press, FeatureFlags.VANILLA_SET);
    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static ChassisBlock chassis;
    public static MachineBlock grinder;
    public static MachineBlock press;
    public static Block sink;
    public static Block churn;
    public static BlockEntityType<ChassisBlockEntity> chassisEntity;
    public static BlockEntityType<KitchenMachineBlockEntity> grinderEntity;
    public static BlockEntityType<KitchenMachineBlockEntity> pressEntity;
    public static BlockEntityType<KitchenSinkBlockEntity> sinkEntity;
    public static BlockEntityType<MilkChurnBlockEntity> churnEntity;

    private MoreFoodMachines() {
    }

    public static Item item(String name) {
        return ITEMS.get(name);
    }

    public static void registerBlocks(BiConsumer<Identifier, Block> registry) {
        Finishes.register(FINISH, MoreFood.id("item_kitchen_panel"));
        chassis = ChassisBlock.builder(KITCHEN, () -> chassisEntity).finishes(FINISH).build(props("block_kitchen_chassis")
                .mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD).noOcclusion());
        registry.accept(MoreFood.id("block_kitchen_chassis"), chassis);
        grinder = MachineBlock.builder(() -> grinderEntity).tiers(KITCHEN).finishes(FINISH).defaultWorktop(MoreFood.id("item_kitchen_worktop")).light(0)
                .build(props("block_grinder")
                .mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());
        registry.accept(MoreFood.id("block_grinder"), grinder);
        press = MachineBlock.builder(() -> pressEntity).tiers(KITCHEN).finishes(FINISH).defaultWorktop(MoreFood.id("item_kitchen_worktop")).light(0).height(2)
                .build(props("block_press").mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());
        registry.accept(MoreFood.id("block_press"), press);
        sink = new KitchenSinkBlock(props("block_kitchen_sink")
                .mapColor(MapColor.METAL).strength(2.5F).sound(SoundType.METAL).noOcclusion());
        registry.accept(MoreFood.id("block_kitchen_sink"), sink);
        churn = new MilkChurnBlock(props("block_milk_churn")
                .mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD).noOcclusion());
        registry.accept(MoreFood.id("block_milk_churn"), churn);
    }

    public static void registerItems(BiConsumer<Identifier, Item> registry) {
        block(registry, "block_kitchen_chassis", chassis);
        block(registry, "block_kitchen_sink", sink);
        block(registry, "block_milk_churn", churn);
        part(registry, "item_grinder_core");
        part(registry, "item_press_core");
        part(registry, "item_kitchen_gearbox");
        part(registry, "item_kitchen_panel");
        part(registry, "item_kitchen_worktop");
        part(registry, "item_firebox");
        // finished machines only come from a chassis, so they stay out of the tab
        registry.accept(MoreFood.id("block_grinder"), new KitchenMachineItem(grinder, MoreFoodItems.withId("block_grinder", MoreFoodItems.props().useBlockDescriptionPrefix())));
        registry.accept(MoreFood.id("block_press"), new KitchenMachineItem(press, MoreFoodItems.withId("block_press", MoreFoodItems.props().useBlockDescriptionPrefix())));

        put("item_ground_soy", MoreFoodItems.item(registry, "item_ground_soy", MoreFoodItems.props()));
        put("item_chickpea_paste", MoreFoodItems.item(registry, "item_chickpea_paste", MoreFoodItems.props()));
        put("item_oat_flour", MoreFoodItems.item(registry, "item_oat_flour", MoreFoodItems.props()));
        put("item_peanut_butter", MoreFoodItems.food(registry, "item_peanut_butter", 3, 0.3F, MoreFoodItems.props()));
        put("item_soy_milk", MoreFoodItems.food(registry, "item_soy_milk", 2, 0.3F, MoreFoodItems.props().usingConvertsTo(Items.GLASS_BOTTLE)));
        put("item_oat_milk", MoreFoodItems.food(registry, "item_oat_milk", 2, 0.3F, MoreFoodItems.props().usingConvertsTo(Items.GLASS_BOTTLE)));
        put("item_olive_oil", MoreFoodItems.item(registry, "item_olive_oil", MoreFoodItems.props().craftRemainder(Items.GLASS_BOTTLE)));
        put("item_vegetable_oil", MoreFoodItems.item(registry, "item_vegetable_oil", MoreFoodItems.props().craftRemainder(Items.GLASS_BOTTLE)));
        put("item_coconut_milk", MoreFoodItems.item(registry, "item_coconut_milk", MoreFoodItems.props().craftRemainder(Items.GLASS_BOTTLE)));
        put("item_tofu", MoreFoodItems.food(registry, "item_tofu", 3, 0.4F, MoreFoodItems.props()));
        put("item_tempeh", MoreFoodItems.food(registry, "item_tempeh", 4, 0.5F, MoreFoodItems.props()));
        put("item_seitan", MoreFoodItems.food(registry, "item_seitan", 2, 0.3F, MoreFoodItems.props()));
        put("item_seitan_fried", MoreFoodItems.food(registry, "item_seitan_fried", 6, 0.6F, MoreFoodItems.props()));
        put("item_falafel_uncooked", MoreFoodItems.item(registry, "item_falafel_uncooked", MoreFoodItems.props()));
        put("item_falafel", MoreFoodItems.food(registry, "item_falafel", 4, 0.5F, MoreFoodItems.props()));
        put("item_hummus", MoreFoodItems.bowl(registry, "item_hummus", 5, 0.5F));
        put("item_guacamole", MoreFoodItems.bowl(registry, "item_guacamole", 4, 0.5F));
        put("item_satay", MoreFoodItems.bowl(registry, "item_satay", 9, 0.8F));
        put("item_pulled_jackfruit", MoreFoodItems.bowl(registry, "item_pulled_jackfruit", 8, 0.7F));
        put("item_butter_vegan", MoreFoodItems.food(registry, "item_butter_vegan", 3, 0.1F, MoreFoodItems.props()));
        put("item_oatcake", MoreFoodItems.food(registry, "item_oatcake", 3, 0.3F, MoreFoodItems.props()));
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> registry) {
        chassisEntity = Registration.blockEntity((pos, state) -> new ChassisBlockEntity(chassisEntity, pos, state), Set.of(chassis));
        grinderEntity = Registration.blockEntity((pos, state) -> new KitchenMachineBlockEntity(grinderEntity, KitchenMachineBlockEntity.Kind.GRINDER, pos, state), Set.of(grinder));
        pressEntity = Registration.blockEntity((pos, state) -> new KitchenMachineBlockEntity(pressEntity, KitchenMachineBlockEntity.Kind.PRESS, pos, state), Set.of(press));
        sinkEntity = Registration.blockEntity(KitchenSinkBlockEntity::new, Set.of(sink));
        registry.accept(MoreFood.id("kitchen_chassis"), chassisEntity);
        registry.accept(MoreFood.id("grinder"), grinderEntity);
        registry.accept(MoreFood.id("press"), pressEntity);
        registry.accept(MoreFood.id("kitchen_sink"), sinkEntity);
        churnEntity = Registration.blockEntity(MilkChurnBlockEntity::new, Set.of(churn));
        registry.accept(MoreFood.id("milk_churn"), churnEntity);

        Assembly.registerCore(MoreFood.id("item_grinder_core"), MachineCore.builder(MoreFood.id("grinder"), () -> grinder)
                .upgradeSlots(2).defaultDrive(Drives.CRANK).build());
        Assembly.registerCore(MoreFood.id("item_press_core"), MachineCore.builder(MoreFood.id("press"), () -> press)
                .upgradeSlots(2).defaultDrive(Drives.CRANK).build());
        PowerParts.register(PowerPart.builder(MoreFood.id("item_kitchen_gearbox")).drive(Drives.CRANK).voltage(Voltage.LV).build());
        Machines.expose(() -> grinderEntity);
        Machines.expose(() -> pressEntity);
        FluidTanks.expose(() -> sinkEntity, (sink, side) -> sink.fluidSides.on(sink.water, side));
    }

    public static void registerMenus(BiConsumer<Identifier, MenuType<?>> registry) {
        registry.accept(MoreFood.id("grinder"), GRINDER_MENU);
        registry.accept(MoreFood.id("press"), PRESS_MENU);
    }

    private static void put(String name, Item item) {
        ITEMS.put(name, item);
    }

    private static void part(BiConsumer<Identifier, Item> registry, String name) {
        put(name, MoreFoodItems.item(registry, name, MoreFoodItems.props().stacksTo(16)));
    }

    private static void block(BiConsumer<Identifier, Item> registry, String name, Block block) {
        put(name, MoreFoodItems.add(registry, name, new BlockItem(block, MoreFoodItems.withId(name, MoreFoodItems.props().useBlockDescriptionPrefix()))));
    }

    private static BlockBehaviour.Properties props(String name) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, MoreFood.id(name)));
    }
}
