package xyz.brckts.portablestonecutter.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryUtils {
    public static int getFirstAvailableSlot(IInventory inventory) { ;
        for (int i = 0; i < 36; ++i) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static void addOrDrop(PlayerEntity player, ItemStack output, int inputCount) {
        for (int i = getFirstAvailableSlot(player.inventory); i != -1 && inputCount > 0; i = getFirstAvailableSlot(player.inventory)) {
            player.addItemStackToInventory(new ItemStack(output.getItem(), (inputCount > (64 / output.getCount()) ? 64 : inputCount * output.getCount())));
            inputCount -= 64 / output.getCount();
        }

        while (inputCount > 0) {
            player.dropItem(new ItemStack(output.getItem(), (inputCount > (64 / output.getCount()) ? 64 : inputCount * output.getCount())), true, true);
            inputCount -= 64 / output.getCount();
        }
    }
}
