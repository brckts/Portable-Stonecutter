package xyz.brckts.portablestonecutter.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryUtils {
    public static int getFirstAvailableSlot(Container inventory) {
        for (int i = 0; i < 36; ++i) {
            if (inventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static void addOrDrop(Player player, ItemStack output, int inputCount) {
        for (int i = getFirstAvailableSlot(player.getInventory()); i != -1 && inputCount > 0; i = getFirstAvailableSlot(player.getInventory())) {
            player.addItem(new ItemStack(output.getItem(), (inputCount > (64 / output.getCount()) ? 64 : inputCount * output.getCount())));
            inputCount -= 64 / output.getCount();
        }

        while (inputCount > 0) {
            player.drop(new ItemStack(output.getItem(), (inputCount > (64 / output.getCount()) ? 64 : inputCount * output.getCount())), true, true);
            inputCount -= 64 / output.getCount();
        }
    }
}
