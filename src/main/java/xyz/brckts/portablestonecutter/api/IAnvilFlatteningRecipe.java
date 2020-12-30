package xyz.brckts.portablestonecutter.api;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import xyz.brckts.portablestonecutter.PortableStonecutter;

import javax.annotation.Nonnull;

public interface IAnvilFlatteningRecipe extends IRecipe<RecipeWrapper> {
    ResourceLocation TYPE_ID = new ResourceLocation(PortableStonecutter.MOD_ID, "anvil_flattening");

    @Nonnull
    @Override
    default IRecipeType<?> getType() {
        return Registry.RECIPE_TYPE.getOptional(TYPE_ID).get();
    }

    @Override
    default boolean canFit(int width, int height) {
        return false;
    }
}
