package xyz.brckts.portablestonecutter.items.crafting;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.api.IAnvilFlatteningRecipe;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnvilFlatteningCraftingManager {

    public static void craft(World world, BlockPos pos) {
        if (world.isRemote()) {
            return;
        }

        List<ItemEntity> itemEntityList = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(pos));
        NonNullList<ItemStack> itemStacks = NonNullList.create();

        for(ItemEntity ie : itemEntityList) {
            itemStacks.add(ie.getItem());
        }


        RecipeWrapper inv = new RecipeWrapper(new ItemStackHandler(itemStacks));

        Optional<IAnvilFlatteningRecipe> recipeOptional = world.getRecipeManager().getRecipe(ModRecipeTypes.ANVIL_FLATTENING_TYPE, inv, world);

        if(!recipeOptional.isPresent()) {
            // No recipe for inputs
            return;
        }

        IAnvilFlatteningRecipe recipe = recipeOptional.get();

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        List<Ingredient> ingredientsMissing = new ArrayList<>(ingredients);
        for(ItemEntity ie : itemEntityList) {
            ItemStack item = ie.getItem();
            for(int i = 0; i < ingredientsMissing.size(); i++) {
                Ingredient ingr = ingredientsMissing.get(i);
                if(ingr.test(item)) {
                    item.setCount(0);
                    ingredientsMissing.remove(i);
                    break;
                }
            }
        }

        world.addEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), recipe.getRecipeOutput()));
    }
}
