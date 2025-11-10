package xyz.brckts.portablestonecutter.items.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record AnvilFlatteningInput(NonNullList<ItemStack> items) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index >= items.size()) {
            throw new IllegalArgumentException("No item for index " + index);
        }

        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
