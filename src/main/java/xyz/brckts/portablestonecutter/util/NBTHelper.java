package xyz.brckts.portablestonecutter.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class NBTHelper {
    private static void clearTags(Player player) {
        ItemStack stack = player.getMainHandItem();
        stack.setTag(null);
    }

    public static StonecutterRecipe getRecipeFromNBT(Level world, CompoundTag nbt) {
        if(nbt == null || !nbt.contains("item") || !nbt.contains("recipeId")) {
            return null;
        }

        int recipeId = nbt.getInt("recipeId");

        Item inputItem = getInputItemFromNBT(nbt);

        Container inputInventory = new SimpleContainer(1);
        inputInventory.setItem(0, new ItemStack(inputItem));

        List<StonecutterRecipe> recipes = world.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, inputInventory, world);

        if(recipeId >= recipes.size()) {
            nbt.remove("recipeId");
            return null;
        }

        return recipes.get(recipeId);
    }

    public static Item getInputItemFromNBT(CompoundTag nbt) {
        if (nbt == null || !nbt.contains("item")) {
            return null;
        }

        ResourceLocation inputItemRL = new ResourceLocation(nbt.getString("item"));

        if(!ForgeRegistries.ITEMS.containsKey(inputItemRL)) {
            nbt.remove("item");
            return null;
        }

        return ForgeRegistries.ITEMS.getValue(inputItemRL);
    }
}
