package xyz.brckts.portablestonecutter.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.StonecutterContainer;

public class PortableStonecutterContainer extends StonecutterContainer {
    public PortableStonecutterContainer(int windowIdIn, PlayerInventory playerInventoryIn) {
        super(windowIdIn, playerInventoryIn);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        playerIn.addItemStackToInventory(this.inputInventory.removeStackFromSlot(0));
        playerIn.inventory.markDirty();
        super.onContainerClosed(playerIn);
    }
}
