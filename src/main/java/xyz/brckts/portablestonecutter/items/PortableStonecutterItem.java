package xyz.brckts.portablestonecutter.items;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if(world.isClientSide()) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        ItemStack is = event.getItemStack();

        if(!(is.getItem() instanceof PortableStonecutterItem || is.getItem() instanceof EnderPortableStonecutterItem)) {
            return;
        }

        CompoundNBT nbt = is.getTag();

        if(nbt == null || !nbt.contains("item") || !nbt.contains("recipeId")) {
            is.setTag(null);
            return;
        }

        int recipeId = nbt.getInt("recipeId");
        ResourceLocation inputItemRL = new ResourceLocation(nbt.getString("item"));

        if(!GameRegistry.findRegistry(Item.class).containsKey(inputItemRL)) {
            is.setTag(null);
            return;
        }

        Item inputItem = GameRegistry.findRegistry(Item.class).getValue(inputItemRL);
        IInventory inputInventory = new Inventory(1);
        inputInventory.setItem(0, new ItemStack(inputItem));

        if(!world.getBlockState(pos).getBlock().equals(Block.byItem(inputItem))) {
            return;
        }

        List<StonecuttingRecipe> recipes = world.getRecipeManager().getRecipesFor(IRecipeType.STONECUTTING, inputInventory, world);
        StonecuttingRecipe recipe = recipes.get(recipeId);
        Block outputBlock = Block.byItem(recipe.getResultItem().getItem());
        int outputCnt = recipe.getResultItem().getCount();

        if(recipeId >= recipes.size()) {
            is.setTag(null);
            return;
        }

        if(outputBlock == Blocks.AIR) {
            return;
        }

        BlockRayTraceResult blockRayTraceResult = getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.ANY);

        List<BlockPos> toReplace;

        if (is.getItem() instanceof EnderPortableStonecutterItem) {
            Direction.Axis axis = blockRayTraceResult.getDirection().getAxis();
            Stream<BlockPos> toReplaceStream;

            switch (axis) {
                case X:
                    PortableStonecutter.LOGGER.debug("AXIS IS X");
                    toReplaceStream = BlockPos.betweenClosedStream(pos.offset(0, -1, -1), pos.offset(0, 1, 1));
                    break;
                case Y:
                    PortableStonecutter.LOGGER.debug("AXIS IS Y");
                    toReplaceStream = BlockPos.betweenClosedStream(pos.offset(-1, 0, -1), pos.offset(1, 0, 1));
                    break;
                case Z:
                    PortableStonecutter.LOGGER.debug("AXIS IS Z");
                    toReplaceStream = BlockPos.betweenClosedStream(pos.offset(-1, -1, 0), pos.offset(1, 1, 0));
                    break;
                default:
                    toReplaceStream = BlockPos.betweenClosedStream(pos, pos);
                    break;
            }
            toReplace = toReplaceStream.map(BlockPos::immutable)
                    .filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(Block.byItem(inputItem)))
                    .collect(Collectors.toList());
        } else {
            toReplace = new ArrayList<>();
            toReplace.add(pos);
        }


        BlockState outputState = outputBlock.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, blockRayTraceResult)));

        for (BlockPos blockPos : toReplace) {
            world.setBlockAndUpdate(blockPos, outputState);
            world.levelEvent(2001, blockPos, Block.getId(outputState));

            if (outputCnt > 1) {
                player.drop(new ItemStack(recipe.getResultItem().getItem(), outputCnt - 1), true, true);
            }
        }
    }

    private static void clearTags(PlayerEntity player) {
        ItemStack stack = player.getMainHandItem();
        stack.setTag(null);
    }


    public INamedContainerProvider getContainer(World worldIn, PlayerEntity playerIn) {
        return new SimpleNamedContainerProvider((id, inventory, player) -> new PortableStonecutterContainer(id, inventory), CONTAINER_NAME);
    }
}