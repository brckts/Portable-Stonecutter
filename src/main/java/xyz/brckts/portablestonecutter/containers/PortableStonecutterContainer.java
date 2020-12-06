package xyz.brckts.portablestonecutter.containers;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.List;

public class PortableStonecutterContainer extends Container {

    private Runnable inventoryUpdateListener = () -> {
    };
    public final IInventory inputInventory = new Inventory(1) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void markDirty() {
            super.markDirty();
            PortableStonecutterContainer.this.onCraftMatrixChanged(this);
            PortableStonecutterContainer.this.inventoryUpdateListener.run();
        }
    };
    private List<StonecuttingRecipe> recipes = Lists.newArrayList();
    private final IntReferenceHolder selectedRecipe = IntReferenceHolder.single();
    private final World world;
    private ItemStack itemStackInput = ItemStack.EMPTY;
    /** The inventory that stores the output of the crafting recipe. */
    private final CraftResultInventory inventory = new CraftResultInventory();
    final Slot inputInventorySlot;
    /** The inventory slot that stores the output of the crafting recipe. */
    final Slot outputInventorySlot;

    public PortableStonecutterContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this(windowId, playerInventory);
    }
    public PortableStonecutterContainer(int windowIdIn, PlayerInventory playerInventoryIn) {
        super(RegistryHandler.PORTABLE_STONECUTTER_CONTAINER.get(), windowIdIn);

        int startX = 8, inY = 8;
        int outY = 45;
        int slotSize = 16;
        int startPlayerInvY = 84;
        int hotbarY = 142;
        this.world = playerInventoryIn.player.world;
        this.inputInventorySlot = this.addSlot(new Slot(inputInventory, 0, startX + 4, inY + 4));
        this.outputInventorySlot = this.addSlot(new Slot(inventory, 1, startX + 4, outY + 4) {
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
                stack.onCrafting(thePlayer.world, thePlayer, stack.getCount());
                PortableStonecutterContainer.this.inventory.onCrafting(thePlayer);
                ItemStack itemstack = PortableStonecutterContainer.this.inputInventorySlot.decrStackSize(1);
                if (!itemstack.isEmpty()) {
                    PortableStonecutterContainer.this.updateRecipeResultSlot();
                }
                return super.onTake(thePlayer, stack);
            }

        });


        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventoryIn, 9 + row * 9 + column, startX + column * (slotSize + 2), startPlayerInvY + row * (slotSize + 2)));
            }
        }

        for(int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventoryIn, column, startX + column * (slotSize + 2), hotbarY));
        }

        this.trackInt(this.selectedRecipe);
    }

    private void updateRecipeResultSlot() {
        if (!this.recipes.isEmpty() && this.isRecipeIdValid(this.selectedRecipe.get())) {
            StonecuttingRecipe stonecuttingrecipe = this.recipes.get(this.selectedRecipe.get());
            this.inventory.setRecipeUsed(stonecuttingrecipe);
            this.outputInventorySlot.putStack(stonecuttingrecipe.getCraftingResult(this.inputInventory));
        } else {
            this.outputInventorySlot.putStack(ItemStack.EMPTY);
        }

        this.detectAndSendChanges();
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        playerIn.addItemStackToInventory(this.inputInventory.removeStackFromSlot(0));
        playerIn.inventory.markDirty();
        super.onContainerClosed(playerIn);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return playerIn.getHeldItemMainhand().isItemEqual(new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get()));
    }

    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            Item item = itemstack1.getItem();
            itemstack = itemstack1.copy();
            if (index == 1) {
                item.onCreated(itemstack1, playerIn.world, playerIn);
                if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index == 0) {
                if (!this.mergeItemStack(itemstack1, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.world.getRecipeManager().getRecipe(IRecipeType.STONECUTTING, new Inventory(itemstack1), this.world).isPresent()) {
                if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 2 && index < 29) {
                if (!this.mergeItemStack(itemstack1, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 29 && index < 38 && !this.mergeItemStack(itemstack1, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }

            slot.onSlotChanged();
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
            this.detectAndSendChanges();
        }

        return itemstack;
    }

    public boolean selectRecipe(PlayerEntity playerIn, int recipeId) {
        if (this.isRecipeIdValid(recipeId)) {
            this.selectedRecipe.set(recipeId);
            this.updateRecipeResultSlot();
        }

        return true;
    }

    public void craftAll(PlayerEntity player) {
        /* repeatedly move valid stacks to input and craft with them would be better idk */
        if (isRecipeIdValid(this.selectedRecipe.get())) {
            int inputItems = 0;
            StonecuttingRecipe recipe = this.recipes.get(this.selectedRecipe.get());
            ItemStack output = recipe.getRecipeOutput();
            PlayerInventory inventory = player.inventory;
            for (int i = 0; i < inventory.getSizeInventory(); ++i) {
                if (isIngredientForSelected(inventory.getStackInSlot(i))) {
                    inputItems += inventory.getStackInSlot(i).getCount();
                    inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                }
            }

            while(inputItems > 0) {
                int consumed = (inputItems > 64 ? 64 : inputItems);
                int toGive = consumed * output.getCount();

                while (toGive > 0) {
                    player.addItemStackToInventory(new ItemStack(output.getItem(), (toGive > 64 ? 64 : toGive)));
                    toGive -= 64;
                }

                inputItems -= consumed;
            }
            player.inventory.markDirty();
        }
    }

    private boolean isIngredientForSelected(ItemStack is) {
        for(ItemStack ingredient : this.recipes.get(this.selectedRecipe.get()).getIngredients().get(0).getMatchingStacks()) {
            if (is.isItemEqual(ingredient))
                return true;
        }
        return false;
    }

    private boolean isRecipeIdValid(int recipeId) {
        return recipeId >= 0 && recipeId < this.recipes.size();
    }

    @OnlyIn(Dist.CLIENT)
    public int getSelectedRecipe() {
        return this.selectedRecipe.get();
    }

    @OnlyIn(Dist.CLIENT)
    public List<StonecuttingRecipe> getRecipeList() {
        return this.recipes;
    }

    @OnlyIn(Dist.CLIENT)
    public int getRecipeListSize() {
        return this.recipes.size();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasItemsinInputSlot() {
        return this.inputInventorySlot.getHasStack() && !this.recipes.isEmpty();
    }

    public void onCraftMatrixChanged(IInventory inventoryIn) {
        ItemStack itemstack = this.inputInventorySlot.getStack();
        if (itemstack.getItem() != this.itemStackInput.getItem()) {
            this.itemStackInput = itemstack.copy();
            this.updateAvailableRecipes(inventoryIn, itemstack);
        }

    }

    private void updateAvailableRecipes(IInventory inventoryIn, ItemStack stack) {
        this.recipes.clear();
        this.selectedRecipe.set(-1);
        this.outputInventorySlot.putStack(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            this.recipes = this.world.getRecipeManager().getRecipes(IRecipeType.STONECUTTING, inventoryIn, this.world);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void setInventoryUpdateListener(Runnable listenerIn) {
        this.inventoryUpdateListener = listenerIn;
    }


}
