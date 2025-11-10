package xyz.brckts.portablestonecutter.containers;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.items.PortableStonecutterItem;
import xyz.brckts.portablestonecutter.network.MessageLockRecipe;
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
    private List<RecipeHolder<StonecutterRecipe>> recipes = Lists.newArrayList();
    private final DataSlot selectedRecipeIndex = DataSlot.standalone();
    private final Level world;
    private ItemStack input = ItemStack.EMPTY;
    /** The inventory that stores the output of the crafting recipe. */
    private final ResultContainer inventory = new ResultContainer();
    final Slot inputSlot;
    /** The inventory slot that stores the output of the crafting recipe. */
    final Slot outputInventorySlot;
    private boolean recipeLocked;
    private RecipeHolder<StonecutterRecipe> lockedRecipe;
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

        this.world = playerInventoryIn.player.level();

        PortableStonecutterItem.Data data = PortableStonecutterItem.Data.get(playerInventoryIn.getSelected());
        Holder<Item> inputItem = data.input();
        RecipeHolder<StonecutterRecipe> recipe = data.recipe(this.world);

        if (inputItem == null || recipe == null) {
            this.lockedRecipe = null;
            this.lockedInput = null;
            this.recipeLocked = false;
        } else {
            this.lockedRecipe = recipe;
            this.lockedInput = inputItem.value();
            this.recipeLocked = true;
        }

        this.inputSlot = this.addSlot(new Slot(this.container, 0, startX + 4, inY + 4));
        this.outputInventorySlot = this.addSlot(new Slot(this.resultContainer, 1, startX + 4, outY + 4) {
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            public void onTake(Player thePlayer, ItemStack stack) {
                stack.onCraftedBy(thePlayer.level(), thePlayer, stack.getCount());
                PortableStonecutterContainer.this.inventory.awardUsedRecipes(thePlayer, getRelevantItems());
                ItemStack itemstack = PortableStonecutterContainer.this.inputSlot.remove(1);
                if (!itemstack.isEmpty()) {
                    PortableStonecutterContainer.this.updateRecipeResultSlot();
                }
                super.onTake(thePlayer, stack);
            }

            private List<ItemStack> getRelevantItems() {
                return List.of(PortableStonecutterContainer.this.inputSlot.getItem());
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

        this.addDataSlot(this.selectedRecipeIndex);
    }

    private void updateRecipeResultSlot() {
        if (!this.recipes.isEmpty() && this.isRecipeIdValid(this.selectedRecipeIndex.get())) {
            if (!this.recipes.get(this.selectedRecipeIndex.get()).equals(this.lockedRecipe)) {
                this.setRecipeLocked(false);
                if (this.world.isClientSide())
                    PacketDistributor.sendToServer(new MessageLockRecipe(this.getSelectedRecipeIndex(), false));
            }
            RecipeHolder<StonecutterRecipe> stonecutterrecipe = this.recipes.get(this.selectedRecipeIndex.get());
            this.inventory.setRecipeUsed(stonecutterrecipe);
            this.outputInventorySlot.set(stonecutterrecipe.value().assemble(new SingleRecipeInput(this.container.getItem(0)), this.world.registryAccess()));
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
                item.onCraftedBy(itemstack1, playerIn.level(), playerIn);
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.world.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, new SingleRecipeInput(itemstack1), this.world).isPresent()) {
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
            this.selectedRecipeIndex.set(recipeId);
            this.updateRecipeResultSlot();
            return true;
        }
        return false;
    }

    public void craftAll(Player player) {

        ItemStack output;
        Item input;

        if(!isRecipeIdValid(this.selectedRecipeIndex.get())) {
            if (this.recipeLocked) {
                output = this.lockedRecipe.value().getResultItem(this.world.registryAccess());
                input = this.lockedInput;            }
            else return;
        } else {
            output = this.recipes.get(this.selectedRecipeIndex.get()).value().getResultItem(this.world.registryAccess());
            input = this.input.getItem();
        }

        ItemStack inputStack = new ItemStack(input);

        int inputCnt = inputSlot.getItem().getCount();
        for(ItemStack itemStack : player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(itemStack, inputStack)) {
                inputCnt += itemStack.getCount();
                itemStack.setCount(0);
            }
        }

        inputSlot.set(ItemStack.EMPTY);
        addOrDrop(player, output, inputCnt);
        this.updateRecipeResultSlot();
        player.getInventory().setChanged();
    }

    public void craft64(Player player) {

        ItemStack output;
        Item input;

        if(!isRecipeIdValid(this.selectedRecipeIndex.get())) {
            if (this.recipeLocked) {
                output = this.lockedRecipe.value().getResultItem(this.world.registryAccess());
                input = this.lockedInput;
            }
            else return;
        } else {
            output = this.recipes.get(this.selectedRecipeIndex.get()).value().getResultItem(this.world.registryAccess());
            input = this.input.getItem();
        }

        ItemStack inputStack = new ItemStack(input);

        int toConvert = 64;
        for (int i = 0; i < player.getInventory().getContainerSize() && toConvert > 0 ; ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if(ItemStack.isSameItemSameComponents(stack, inputStack)) {
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
            if(inputSlot.getItem().getCount() > toConvert) {
                inputSlot.remove(toConvert);
                toConvert = 0;
            } else {
                toConvert -= inputSlot.getItem().getCount();
                inputSlot.set(ItemStack.EMPTY);
            }
        }
        addOrDrop(player, output, 64 - toConvert);
        this.updateRecipeResultSlot();
        player.getInventory().setChanged();
    }

    public void onRecipeLocked(int recipeId, ServerPlayer player) {
        if (recipeId != this.selectedRecipeIndex.get()) {
            return;
        }

        if (!this.recipeLocked)  {
            return;
        }

        ItemStack pScStack = player.getMainHandItem();
        Item inputItem = this.input.getItem();

        this.updateLockData(inputItem, this.recipes.get(recipeId));

        PortableStonecutterItem.Data data = PortableStonecutterItem.Data.get(pScStack);
        data = data.withInput(BuiltInRegistries.ITEM.getHolder(BuiltInRegistries.ITEM.getKey(inputItem)).orElse(null));
        data = data.withRecipe(this.recipes.get(recipeId));
        PortableStonecutterItem.Data.set(pScStack, data);
    }

    public void updateLockData(Item inputItem, RecipeHolder<StonecutterRecipe> recipe) {
        this.lockedInput = inputItem;
        this.lockedRecipe = recipe;
    }

    public void onRecipeUnlocked(ServerPlayer player) {
        if(this.recipeLocked) {
            return;
        }

        this.lockedInput = null;
        this.lockedRecipe = null;

        ItemStack itemStack = player.getMainHandItem();

        PortableStonecutterItem.Data data = PortableStonecutterItem.Data.get(itemStack);
        data = data.withInput(null);
        data = data.withRecipe(null);
        PortableStonecutterItem.Data.set(itemStack, data);
    }

    public boolean isRecipeIdValid(int recipeId) {
        return recipeId >= 0 && recipeId < this.recipes.size();
    }

    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex.get();
    }

    public List<RecipeHolder<StonecutterRecipe>> getRecipeList() {
        return this.recipes;
    }

    public int getNumRecipes() {
        return this.recipes.size();
    }

    public boolean hasInputItem() {
        return this.inputSlot.hasItem() && !this.recipes.isEmpty();
    }

    public RecipeHolder<StonecutterRecipe> getLockedRecipe() {
        return this.lockedRecipe;
    }

    public Item getLockedInput() {
        return this.lockedInput;
    }

    public void slotsChanged(Container inventoryIn) {
        ItemStack itemstack = this.inputSlot.getItem();
        if (!itemstack.is(this.input.getItem())) {
            this.input = itemstack.copy();
            this.updateAvailableRecipes(inventoryIn, itemstack);
        }
    }

    private void updateAvailableRecipes(Container inventoryIn, ItemStack stack) {
        this.recipes.clear();
        this.selectedRecipeIndex.set(-1);
        this.outputInventorySlot.set(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            this.recipes = this.world.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, new SingleRecipeInput(inventoryIn.getItem(0)), this.world);
            if (stack.getItem().equals(this.lockedInput)) {
                this.selectedRecipeIndex.set(this.recipes.indexOf(this.lockedRecipe));
                this.updateRecipeResultSlot();
            } else {
                this.recipeLocked = false;
            }
        }
    }

    public void setRecipeLocked(boolean lock) {
        if (lock) {
            this.recipeLocked = isLockable();
            if (this.recipeLocked) updateLockData(this.input.getItem(), this.recipes.get(this.selectedRecipeIndex.get()));
        } else {
            this.recipeLocked = false;
        }
    }

    public boolean isLockable() {
        return this.isRecipeIdValid(this.selectedRecipeIndex.get()) && Block.byItem(this.recipes.get(this.selectedRecipeIndex.get()).value().getResultItem(this.world.registryAccess()).getItem()) != Blocks.AIR;
    }

    public boolean isRecipeLocked() {
        return this.recipeLocked;
    }

    @OnlyIn(Dist.CLIENT)
    public void setInventoryUpdateListener(Runnable listenerIn) {
        this.slotUpdateListener = listenerIn;
    }


}
