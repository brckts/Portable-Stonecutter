package xyz.brckts.portablestonecutter.containers;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brckts.portablestonecutter.items.PortableStonecutterItem;
import xyz.brckts.portablestonecutter.network.MessageLockRecipe;
import xyz.brckts.portablestonecutter.network.NetworkHandler;
import xyz.brckts.portablestonecutter.util.NBTHelper;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.List;
import java.util.Objects;

import static xyz.brckts.portablestonecutter.util.InventoryUtils.addOrDrop;

public class PortableStonecutterContainer extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 1;
    private static final int INV_SLOT_START = 2;
    private static final int INV_SLOT_END = 29;
    private static final int USE_ROW_SLOT_START = 29;
    private static final int USE_ROW_SLOT_END = 38;
    private List<StonecutterRecipe> recipes = Lists.newArrayList();
    private final DataSlot selectedRecipe = DataSlot.standalone();
    private final Level world;
    private ItemStack itemStackInput = ItemStack.EMPTY;
    /** The inventory that stores the output of the crafting recipe. */
    private final ResultContainer inventory = new ResultContainer();
    final Slot inputInventorySlot;
    /** The inventory slot that stores the output of the crafting recipe. */
    final Slot outputInventorySlot;
    private boolean recipeLocked;
    private StonecutterRecipe lockedRecipe;
    private Item lockedInput;

    private Runnable slotUpdateListener = () -> {
    };
    public final Container container = new SimpleContainer(1) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void setChanged() {
            super.setChanged();
            PortableStonecutterContainer.this.slotsChanged(this);
            PortableStonecutterContainer.this.slotUpdateListener.run();
        }
    };
    final ResultContainer resultContainer = new ResultContainer();

    public PortableStonecutterContainer(int windowIdIn, Inventory playerInventoryIn) {
        super(RegistryHandler.PORTABLE_STONECUTTER_CONTAINER.get(), windowIdIn);

        int startX = 8, inY = 8;
        int outY = 45;
        int slotSize = 16;
        int startPlayerInvY = 84;
        int hotbarY = 142;

        CompoundTag nbt = playerInventoryIn.getSelected().getTag();

        if (nbt == null || !nbt.contains("item") || !nbt.contains("recipeId")) {
            this.recipeLocked = false;
            this.lockedRecipe = null;
        } else {
            this.lockedRecipe = NBTHelper.getRecipeFromNBT(playerInventoryIn.player.level, nbt);
            this.lockedInput = NBTHelper.getInputItemFromNBT(nbt);
            if (lockedInput != null && lockedRecipe != null) this.recipeLocked = true;
        }

        this.world = playerInventoryIn.player.level;
        this.inputInventorySlot = this.addSlot(new Slot(this.container, 0, startX + 4, inY + 4));
        this.outputInventorySlot = this.addSlot(new Slot(this.resultContainer, 1, startX + 4, outY + 4) {
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            public void onTake(Player thePlayer, ItemStack stack) {
                stack.onCraftedBy(thePlayer.level, thePlayer, stack.getCount());
                PortableStonecutterContainer.this.inventory.awardUsedRecipes(thePlayer);
                ItemStack itemstack = PortableStonecutterContainer.this.inputInventorySlot.remove(1);
                if (!itemstack.isEmpty()) {
                    PortableStonecutterContainer.this.updateRecipeResultSlot();
                }
                super.onTake(thePlayer, stack);
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
            if (!this.recipes.get(this.selectedRecipe.get()).equals(this.lockedRecipe)) {
                this.setRecipeLocked(false);
                if (this.world.isClientSide())
                    NetworkHandler.channel.sendToServer(new MessageLockRecipe(this.getSelectedRecipe(), false));
            }
            StonecutterRecipe stonecutterrecipe = this.recipes.get(this.selectedRecipe.get());
            this.inventory.setRecipeUsed(stonecutterrecipe);
            this.outputInventorySlot.set(stonecutterrecipe.assemble(this.container, this.world.registryAccess()));
        } else {
            this.outputInventorySlot.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    @Override
    public void removed(Player playerIn) {
        playerIn.addItem(this.container.removeItemNoUpdate(0));
        playerIn.getInventory().setChanged();
        super.removed(playerIn);
    }



    @Override
    public boolean stillValid(Player playerIn) {
        return playerIn.getMainHandItem().getItem() instanceof PortableStonecutterItem;
    }

    public ItemStack quickMoveStack(Player playerIn, int index) {
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
            } else if (this.world.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, new SimpleContainer(itemstack1), this.world).isPresent()) {
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

    public void craftAll(Player player) {

        ItemStack output;
        Item input;

        if(!isRecipeIdValid(this.selectedRecipe.get())) {
            if (this.recipeLocked) {
                output = this.lockedRecipe.getResultItem(this.world.registryAccess());
                input = this.lockedInput;            }
            else return;
        } else {
            output = this.recipes.get(this.selectedRecipe.get()).getResultItem(this.world.registryAccess());
            input = this.itemStackInput.getItem();
        }

        int inputCnt = inputInventorySlot.getItem().getCount();
        for(ItemStack itemStack : player.getInventory().items) {
            if (ItemStack.isSameItemSameTags(itemStack, new ItemStack(input))) {
                inputCnt += itemStack.getCount();
                itemStack.setCount(0);
            }
        }

        inputInventorySlot.set(ItemStack.EMPTY);
        addOrDrop(player, output, inputCnt);
        this.updateRecipeResultSlot();
        player.getInventory().setChanged();
    }

    public void craft64(Player player) {

        ItemStack output;
        Item input;

        if(!isRecipeIdValid(this.selectedRecipe.get())) {
            if (this.recipeLocked) {
                output = this.lockedRecipe.getResultItem(this.world.registryAccess());
                input = this.lockedInput;
            }
            else return;
        } else {
            output = this.recipes.get(this.selectedRecipe.get()).getResultItem(this.world.registryAccess());
            input = this.itemStackInput.getItem();
        }

        int toConvert = 64;
        for (int i = 0; i < player.getInventory().getContainerSize() && toConvert > 0 ; ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if(ItemStack.isSameItemSameTags(stack, new ItemStack(input))) {
                if (toConvert >= stack.getCount()) {
                    toConvert -= stack.getCount();
                    player.getInventory().setItem(i, ItemStack.EMPTY);
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
        player.getInventory().setChanged();
    }

    public void onRecipeLocked(int recipeId, ServerPlayer player) {
        if (recipeId != this.selectedRecipe.get()) {
            return;
        }

        if (!this.recipeLocked)  {
            return;
        }

        ItemStack pScStack = player.getMainHandItem();
        CompoundTag nbtTagCompound = pScStack.getTag();
        Item inputItem = this.itemStackInput.getItem();

        this.updateLockData(inputItem, this.recipes.get(recipeId));

        if (nbtTagCompound == null) {
            nbtTagCompound = new CompoundTag();
            pScStack.setTag(nbtTagCompound);
        }

        nbtTagCompound.putString("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(inputItem)).toString());
        nbtTagCompound.putInt("recipeId", recipeId);
    }

    public void updateLockData(Item inputItem, StonecutterRecipe recipe) {
        this.lockedInput = inputItem;
        this.lockedRecipe = recipe;
    }

    public void onRecipeUnlocked(ServerPlayer player) {
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

    public int getSelectedRecipe() {
        return this.selectedRecipe.get();
    }

    @OnlyIn(Dist.CLIENT)
    public List<StonecutterRecipe> getRecipeList() {
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
    public StonecutterRecipe getLockedRecipe() {
        return this.lockedRecipe;
    }

    @OnlyIn(Dist.CLIENT)
    public Item getLockedInput() {
        return this.lockedInput;
    }

    public void slotsChanged(Container inventoryIn) {
        ItemStack itemstack = this.inputInventorySlot.getItem();
        if (itemstack.getItem() != this.itemStackInput.getItem()) {
            this.itemStackInput = itemstack.copy();
            this.updateAvailableRecipes(inventoryIn, itemstack);
        }

    }

    private void updateAvailableRecipes(Container inventoryIn, ItemStack stack) {
        this.recipes.clear();
        this.selectedRecipe.set(-1);
        this.outputInventorySlot.set(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            this.recipes = this.world.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, inventoryIn, this.world);
            if (stack.getItem().equals(this.lockedInput)) {
                this.selectedRecipe.set(this.recipes.indexOf(this.lockedRecipe));
                this.updateRecipeResultSlot();
            } else {
                this.recipeLocked = false;
            }
        }
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
        return this.isRecipeIdValid(this.selectedRecipe.get()) && Block.byItem(this.recipes.get(this.selectedRecipe.get()).getResultItem(this.world.registryAccess()).getItem()) != Blocks.AIR;
    }

    public boolean isRecipeLocked() {
        return this.recipeLocked;
    }

    @OnlyIn(Dist.CLIENT)
    public void setInventoryUpdateListener(Runnable listenerIn) {
        this.slotUpdateListener = listenerIn;
    }


}
