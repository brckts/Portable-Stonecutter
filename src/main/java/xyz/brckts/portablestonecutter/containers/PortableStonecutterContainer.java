package xyz.brckts.portablestonecutter.containers;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.items.PortableStonecutterItem;
import xyz.brckts.portablestonecutter.util.NBTHelper;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.List;

import static xyz.brckts.portablestonecutter.util.InventoryUtils.addOrDrop;

public class PortableStonecutterContainer extends Container {

    private Runnable inventoryUpdateListener = () -> {
    };
    public final IInventory inputInventory = new Inventory(1) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void setChanged() {
            super.setChanged();
            PortableStonecutterContainer.this.onCraftMatrixChanged(this);
            PortableStonecutterContainer.this.inventoryUpdateListener.run();
        }
    };
    private List<StonecuttingRecipe> recipes = Lists.newArrayList();
    private final IntReferenceHolder selectedRecipe = IntReferenceHolder.standalone();
    private final World world;
    private ItemStack itemStackInput = ItemStack.EMPTY;
    /** The inventory that stores the output of the crafting recipe. */
    private final CraftResultInventory inventory = new CraftResultInventory();
    final Slot inputInventorySlot;
    /** The inventory slot that stores the output of the crafting recipe. */
    final Slot outputInventorySlot;
    private boolean recipeLocked;
    private StonecuttingRecipe lockedRecipe;
    private Item lockedInput;

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

        CompoundNBT nbt = playerInventoryIn.getSelected().getTag();

        if (nbt == null || !nbt.contains("item") || !nbt.contains("recipeId")) {
            this.recipeLocked = false;
            this.lockedRecipe = null;
        } else {
            this.lockedRecipe = NBTHelper.getRecipeFromNBT(playerInventoryIn.player.level, nbt);
            this.lockedInput = NBTHelper.getInputItemFromNBT(nbt);
            if (lockedInput != null && lockedRecipe != null) this.recipeLocked = true;
        }

        this.world = playerInventoryIn.player.level;
        this.inputInventorySlot = this.addSlot(new Slot(inputInventory, 0, startX + 4, inY + 4));
        this.outputInventorySlot = this.addSlot(new Slot(inventory, 1, startX + 4, outY + 4) {
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
                stack.onCraftedBy(thePlayer.level, thePlayer, stack.getCount());
                PortableStonecutterContainer.this.inventory.awardUsedRecipes(thePlayer);
                ItemStack itemstack = PortableStonecutterContainer.this.inputInventorySlot.remove(1);
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

        this.addDataSlot(this.selectedRecipe);
    }

    private void updateRecipeResultSlot() {
        if (!this.recipes.isEmpty() && this.isRecipeIdValid(this.selectedRecipe.get())) {
            StonecuttingRecipe stonecuttingrecipe = this.recipes.get(this.selectedRecipe.get());
            this.inventory.setRecipeUsed(stonecuttingrecipe);
            this.outputInventorySlot.set(stonecuttingrecipe.assemble(this.inputInventory));
        } else {
            this.outputInventorySlot.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    @Override
    public void removed(PlayerEntity playerIn) {
        playerIn.addItem(this.inputInventory.removeItemNoUpdate(0));
        playerIn.inventory.setChanged();
        super.removed(playerIn);
    }



    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return playerIn.getMainHandItem().getItem() instanceof PortableStonecutterItem;
    }

    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            Item item = itemstack1.getItem();
            itemstack = itemstack1.copy();
            if (index == 1) {
                item.onCraftedBy(itemstack1, playerIn.level, playerIn);
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.world.getRecipeManager().getRecipeFor(IRecipeType.STONECUTTING, new Inventory(itemstack1), this.world).isPresent()) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 2 && index < 29) {
                if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 29 && index < 38 && !this.moveItemStackTo(itemstack1, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
            this.broadcastChanges();
        }

        return itemstack;
    }

    public boolean selectRecipe(int recipeId) {
        if (this.isRecipeIdValid(recipeId)) {
            this.selectedRecipe.set(recipeId);
            this.updateRecipeResultSlot();
            return true;
        }
        return false;
    }

    //TODO: If no input item but locked recipe: craft locked recipe
    public void craftAll(PlayerEntity player) {

        if(!isRecipeIdValid(this.selectedRecipe.get())) {
            return;
        }

        ItemStack output = this.recipes.get(this.selectedRecipe.get()).getResultItem();
        int inputCnt = inputInventorySlot.getItem().getCount();
        for(ItemStack itemStack : player.inventory.items) {
            if (itemStack.sameItemStackIgnoreDurability(this.itemStackInput) &&
                    (NBTUtil.compareNbt(itemStackInput.getTag(), itemStack.getTag(), false))) {
                inputCnt += itemStack.getCount();
                itemStack.setCount(0);
            }
        }

        inputInventorySlot.set(ItemStack.EMPTY);
        addOrDrop(player, output, inputCnt);
        this.updateRecipeResultSlot();
        player.inventory.setChanged();
    }

    public void craft64(PlayerEntity player) {

        if(!isRecipeIdValid(this.selectedRecipe.get())) {
            return;
        }

        ItemStack output = this.recipes.get(this.selectedRecipe.get()).getResultItem();

        int toConvert = 64;
        for (int i = 0; i < player.inventory.getContainerSize() && toConvert > 0 ; ++i) {
            ItemStack stack = player.inventory.getItem(i);
            if(this.itemStackInput.sameItemStackIgnoreDurability(stack)) {
                if (toConvert >= stack.getCount()) {
                    toConvert -= stack.getCount();
                    player.inventory.setItem(i, ItemStack.EMPTY);
                } else {
                    stack.setCount(stack.getCount() - toConvert);
                    toConvert = 0;
                }
            }
        }
        if(toConvert > 0) {
            if(inputInventorySlot.getItem().getCount() > toConvert) {
                inputInventorySlot.remove(toConvert);
                toConvert = 0;
            } else {
                toConvert -= inputInventorySlot.getItem().getCount();
                inputInventorySlot.set(ItemStack.EMPTY);
            }
        }
        addOrDrop(player, output, 64 - toConvert);
        this.updateRecipeResultSlot();
        player.inventory.setChanged();
    }

    public void onRecipeLocked(int recipeId, ServerPlayerEntity player) {
        if (recipeId != this.selectedRecipe.get()) {
            return;
        }

        if (!this.recipeLocked)  {
            return;
        }

        ItemStack pScStack = player.getMainHandItem();
        CompoundNBT nbtTagCompound = pScStack.getTag();
        Item inputItem = this.itemStackInput.getItem();

        this.updateLockData(inputItem, this.recipes.get(recipeId));

        if (nbtTagCompound == null) {
            nbtTagCompound = new CompoundNBT();
            pScStack.setTag(nbtTagCompound);
        }

        nbtTagCompound.putString("item", inputItem.getRegistryName().toString());
        nbtTagCompound.putInt("recipeId", recipeId);
    }

    public void updateLockData(Item inputItem, StonecuttingRecipe recipe) {
        this.lockedInput = inputItem;
        this.lockedRecipe = recipe;
    }

    public void onRecipeUnlocked(ServerPlayerEntity player) {
        if(this.recipeLocked) {
            return;
        }

        this.lockedInput = null;
        this.lockedRecipe = null;

        if(player.getMainHandItem().getTag() == null) {
            return;
        }

        player.getMainHandItem().getTag().remove("item");
        player.getMainHandItem().getTag().remove("recipeId");
    }

    public boolean isRecipeIdValid(int recipeId) {
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
    public boolean hasItemsInInputSlot() {
        return this.inputInventorySlot.hasItem() && !this.recipes.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    public StonecuttingRecipe getLockedRecipe() {
        return this.lockedRecipe;
    }

    @OnlyIn(Dist.CLIENT)
    public Item getLockedInput() {
        return this.lockedInput;
    }

    public void onCraftMatrixChanged(IInventory inventoryIn) {
        ItemStack itemstack = this.inputInventorySlot.getItem();
        if (itemstack.getItem() != this.itemStackInput.getItem()) {
            this.itemStackInput = itemstack.copy();
            this.updateAvailableRecipes(inventoryIn, itemstack);
        }

    }

    private void updateAvailableRecipes(IInventory inventoryIn, ItemStack stack) {
        this.recipes.clear();
        this.selectedRecipe.set(-1);
        this.outputInventorySlot.set(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            this.recipes = this.world.getRecipeManager().getRecipesFor(IRecipeType.STONECUTTING, inventoryIn, this.world);
            if (stack.getItem().equals(this.lockedInput)) {
                this.selectedRecipe.set(this.recipes.indexOf(this.lockedRecipe));
                this.updateRecipeResultSlot();
            } else {
                this.recipeLocked = false;
            }
        }
    }

    public void toggleRecipeLock() {
        this.recipeLocked = !this.recipeLocked;
    }

    public void setRecipeLocked(boolean lock) {
        if (lock) {
            this.recipeLocked = isLockable();
            if (this.recipeLocked) updateLockData(this.itemStackInput.getItem(), this.recipes.get(this.selectedRecipe.get()));
        } else {
            this.recipeLocked = false;
        }
    }

    public boolean isLockable() {
        return this.isRecipeIdValid(this.selectedRecipe.get()) && Block.byItem(this.recipes.get(this.selectedRecipe.get()).getResultItem().getItem()) != Blocks.AIR;
    }

    public boolean isRecipeLocked() {
        return this.recipeLocked;
    }

    @OnlyIn(Dist.CLIENT)
    public void setInventoryUpdateListener(Runnable listenerIn) {
        this.inventoryUpdateListener = listenerIn;
    }


}
