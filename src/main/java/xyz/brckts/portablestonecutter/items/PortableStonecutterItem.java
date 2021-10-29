package xyz.brckts.portablestonecutter.items;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.List;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PortableStonecutterItem extends Item {
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.portable_stonecutter");
    public PortableStonecutterItem() {
        super(new Item.Properties().tab(PortableStonecutter.TAB).stacksTo(1));
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(!worldIn.isClientSide()) {
            clearTags(playerIn);
            playerIn.openMenu(this.getContainer(worldIn, playerIn));
            playerIn.awardStat(Stats.INTERACT_WITH_STONECUTTER);
            return super.use(worldIn, playerIn, handIn);
        } else {
            return ActionResult.success(playerIn.getItemInHand(handIn));
        }
    }






    private static void clearTags(PlayerEntity player) {
        ItemStack stack = player.getMainHandItem();
        stack.setTag(null);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if(world.isClientSide()) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        ItemStack is = event.getItemStack();

        if(!is.sameItemStackIgnoreDurability(new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get()))) {
            return;
        }

        CompoundNBT nbt = is.getTag();

        if(nbt == null || !nbt.contains("item") || !nbt.contains("recipeId")) {
            clearTags(player);
            return;
        }

        int recipeId = nbt.getInt("recipeId");
        ResourceLocation inputItemRL = new ResourceLocation(nbt.getString("item"));

        if(!GameRegistry.findRegistry(Item.class).containsKey(inputItemRL)) {
            clearTags(player);
            return;
        }

        Item inputItem = GameRegistry.findRegistry(Item.class).getValue(inputItemRL);
        if(!world.getBlockState(pos).getBlock().equals(Block.byItem(inputItem))) {
            return;
        }

        IInventory inputInventory = new Inventory(1);
        inputInventory.setItem(0, new ItemStack(inputItem));

        List<StonecuttingRecipe> recipes = world.getRecipeManager().getRecipesFor(IRecipeType.STONECUTTING, inputInventory, world);

        if(recipeId >= recipes.size()) {
            clearTags(player);
            return;
        }

        StonecuttingRecipe recipe = recipes.get(recipeId);
        Block outputBlock = Block.byItem(recipe.getResultItem().getItem());
        int outputCnt = recipe.getResultItem().getCount();

        if(outputBlock == Blocks.AIR) {
            return;
        }

        BlockRayTraceResult blockRayTraceResult = getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.ANY);
        world.setBlockAndUpdate(pos, outputBlock.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, blockRayTraceResult))));

        if(outputCnt > 1) {
            player.drop(new ItemStack(recipe.getResultItem().getItem(), outputCnt - 1), true, true);
        }
    }


    public INamedContainerProvider getContainer(World worldIn, PlayerEntity playerIn) {
        return new SimpleNamedContainerProvider((id, inventory, player) -> new PortableStonecutterContainer(id, inventory), CONTAINER_NAME);
    }
}