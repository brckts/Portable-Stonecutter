package xyz.brckts.portablestonecutter.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

public class NBTHelper {
    private static void clearTags(PlayerEntity player) {
        ItemStack stack = player.getMainHandItem();
        stack.setTag(null);
    }

    public static StonecuttingRecipe getRecipeFromNBT(World world, CompoundNBT nbt) {
        if(nbt == null || !nbt.contains("item") || !nbt.contains("recipeId")) {
            return null;
        }

        int recipeId = nbt.getInt("recipeId");

        Item inputItem = getInputItemFromNBT(nbt);

        IInventory inputInventory = new Inventory(1);
        inputInventory.setItem(0, new ItemStack(inputItem));

        List<StonecuttingRecipe> recipes = world.getRecipeManager().getRecipesFor(IRecipeType.STONECUTTING, inputInventory, world);

        if(recipeId >= recipes.size()) {
            nbt.remove("recipeId");
            return null;
        }

        return recipes.get(recipeId);
    }

    public static Item getInputItemFromNBT(CompoundNBT nbt) {
        if (nbt == null || !nbt.contains("item")) {
            return null;
        }

        ResourceLocation inputItemRL = new ResourceLocation(nbt.getString("item"));

        if(!GameRegistry.findRegistry(Item.class).containsKey(inputItemRL)) {
            nbt.remove("item");
            return null;
        }

        return GameRegistry.findRegistry(Item.class).getValue(inputItemRL);
    }
}
