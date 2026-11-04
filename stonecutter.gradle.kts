plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.1.2-fabric"

stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    properties {
        tags(version, loader)
    }

    constants {
        match(loader, "fabric", "neoforge", "forge")
    }

    replacements.string(current.parsed < "1.21.11") {
        replace("Identifier", "ResourceLocation")
    }

    replacements.string(current.parsed < "26.1") {
        replace("import net.fabricmc.fabric.api.registry.CompostableRegistry;", "import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;")
        replace("CompostableRegistry.INSTANCE", "CompostingChanceRegistry.INSTANCE")
        replace("import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;", "import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;")
        replace("ParticleProviderRegistry.getInstance()", "ParticleFactoryRegistry.getInstance()")
        replace(".noCollision()", ".noCollission()")
        replace("popExperience(player.level(), ", "popExperience(player.serverLevel(), ")
        replace("InteractionResult.TRY_WITH_EMPTY_HAND", "InteractionResult .PASS")
        replace("getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction)", "getAnalogOutputSignal(BlockState state, Level level, BlockPos pos)")
        replace(".setId(ResourceKey.create(Registries.BLOCK, ", ".mapColor(com.workshopcraft.morefood.Compat.unnamed(")
        replace("import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;", "import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;")
        replace("ContainerStorage.of(", "InventoryStorage.of(")
        replace("layer.typeHolder()", "layer.getItem().builtInRegistryHolder()")
        replace("Mth.lerpDiscrete(", "com.workshopcraft.morefood.Compat.lerpDiscrete(")
        replace("carried.onCraftedBy(player, taken)", "carried.onCraftedBy(player.level(), player, taken)")
        replace("ingredient.items().map(Holder::value)", "com.workshopcraft.morefood.Compat.items(ingredient)")
        replace("import net.minecraft.world.item.ItemStackTemplate;", "import com.workshopcraft.morefood.legacy.ItemStackTemplate;")
        replace("import net.minecraft.world.level.storage.ValueInput;", "import com.workshopcraft.morefood.legacy.ValueInput;")
        replace("import net.minecraft.world.level.storage.ValueOutput;", "import com.workshopcraft.morefood.legacy.ValueOutput;")
        replace("import net.minecraft.world.item.crafting.RecipeBookCategory;", "import com.workshopcraft.morefood.legacy.RecipeBookCategory;")
        replace("import net.minecraft.client.renderer.SubmitNodeCollector;", "import com.workshopcraft.morefood.legacy.client.SubmitNodeCollector;")
        replace("import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;", "import com.workshopcraft.morefood.legacy.client.BlockEntityRenderState;")
        replace("import net.minecraft.client.renderer.item.ItemStackRenderState;", "import com.workshopcraft.morefood.legacy.client.ItemStackRenderState;")
        replace("import net.minecraft.client.renderer.item.ItemModelResolver;", "import com.workshopcraft.morefood.legacy.client.ItemModelResolver;")
        replace("import net.minecraft.client.renderer.rendertype.RenderTypes;", "import com.workshopcraft.morefood.legacy.client.RenderTypes;")
        replace("import net.minecraft.client.renderer.state.level.CameraRenderState;", "import com.workshopcraft.morefood.legacy.client.CameraRenderState;")
        replace("import net.minecraft.client.resources.model.sprite.SpriteGetter;", "import com.workshopcraft.morefood.legacy.client.SpriteGetter;")
        replace("import net.minecraft.client.resources.model.sprite.SpriteId;", "import com.workshopcraft.morefood.legacy.client.SpriteId;")
        replace("import net.minecraft.util.ARGB;", "import com.workshopcraft.morefood.legacy.client.ARGB;")
        replace("import net.minecraft.client.renderer.feature.ModelFeatureRenderer;", "import com.workshopcraft.morefood.legacy.client.ModelFeatureRenderer;")
        replace("import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;", "import com.workshopcraft.morefood.legacy.client.BlockEntityRenderer;")
        replace("context.sprites()", "SpriteGetter.of(context)")
        replace("context.itemModelResolver()", "ItemModelResolver.of(context)")
        replace("ContainerHelper.loadAllItems(input, items)", "input.loadAllItems(items)")
        replace("ContainerHelper.saveAllItems(output, items)", "output.saveAllItems(items)")
        replace("extends BlockEntity ", "extends com.workshopcraft.morefood.legacy.LegacyBlockEntity ")
        replace("extends BaseContainerBlockEntity ", "extends com.workshopcraft.morefood.legacy.LegacyContainerBlockEntity ")
        replace("extends AbstractCookingRecipe ", "extends com.workshopcraft.morefood.legacy.LegacyCookingRecipe ")
        replace("implements Recipe<", "implements com.workshopcraft.morefood.legacy.LegacyRecipe<")
        replace("Recipe.CommonInfo", "CommonInfo")
        replace("player.sendOverlayMessage(", "com.workshopcraft.morefood.Compat.overlay(player, ")
        replace("stack.limitSize(", "com.workshopcraft.morefood.Compat.limit(stack, ")
        replace("level.fuelValues()", "com.workshopcraft.morefood.Compat.fuel(level)")
        replace("fuelItem.getCraftingRemainder()", "com.workshopcraft.morefood.Compat.remainder(fuelItem)")
        replace("stack.getItem().getCraftingRemainder()", "com.workshopcraft.morefood.Compat.remainder(stack.getItem())")
        replace("level.recipeAccess()", "com.workshopcraft.morefood.legacy.RecipeAccess.of(level)")
        replace("server.recipeAccess()", "com.workshopcraft.morefood.legacy.RecipeAccess.of(server)")
        replace("LevelRenderer.getLightCoords(", "LevelRenderer.getLightColor(")
        replace("ExtraCodecs.STRING_RGB_COLOR", "com.workshopcraft.morefood.Compat.RGB")
    }

    replacements.string(current.parsed < "1.21") {
        replace(".getU(", ".getU(16 * ")
        replace(".getV(", ".getV(16 * ")
        replace("import net.minecraft.world.item.crafting.RecipeInput;", "import com.workshopcraft.morefood.legacy.RecipeInput;")
        replace("import net.minecraft.world.item.crafting.SingleRecipeInput;", "import com.workshopcraft.morefood.legacy.SingleRecipeInput;")
        replace("import net.minecraft.world.item.crafting.RecipeHolder;", "import com.workshopcraft.morefood.legacy.RecipeHolder;")
        replace("Ingredient.CODEC", "com.workshopcraft.morefood.Compat.INGREDIENT")
        replace("ItemStack.isSameItemSameComponents(", "ItemStack.isSameItemSameTags(")
        replace("stack.consume(1, player)", "com.workshopcraft.morefood.Compat.consume(stack, 1, player)")
        replace("context.getItemInHand().consume(1, context.getPlayer())", "com.workshopcraft.morefood.Compat.consume(context.getItemInHand(), 1, context.getPlayer())")
        replace("getMaxStackSize(stack)", "Math.min(getMaxStackSize(), stack.getMaxStackSize())")
        replace(".saturationModifier(", ".saturationMod(")
    }
}
