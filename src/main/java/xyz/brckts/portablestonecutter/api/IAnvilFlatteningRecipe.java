package xyz.brckts.portablestonecutter.api;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import xyz.brckts.portablestonecutter.PortableStonecutter;

import javax.annotation.Nonnull;

public interface IAnvilFlatteningRecipe extends Recipe<RecipeWrapper> {
    ResourceLocation TYPE_ID = new ResourceLocation(PortableStonecutter.MOD_ID, "anvil_flattening");

    @Nonnull
    @Override
    default RecipeType<?> getType() {
        return Registry.RECIPE_TYPE.getOptional(TYPE_ID).get();
    }

    ResourceLocation getAllowedDim();
}
