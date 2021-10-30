package xyz.brckts.portablestonecutter.items;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xyz.brckts.portablestonecutter.util.NBTHelper.getInputItemFromNBT;
import static xyz.brckts.portablestonecutter.util.NBTHelper.getRecipeFromNBT;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PortableStonecutterItem extends Item {
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.portable_stonecutter");
    public PortableStonecutterItem() {
        super(new Item.Properties().tab(PortableStonecutter.TAB).stacksTo(1));
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(!worldIn.isClientSide()) {
            //clearTags(playerIn);
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

        if(!(is.getItem() instanceof PortableStonecutterItem)) {
            return;
        }

        CompoundNBT nbt = is.getTag();
        StonecuttingRecipe recipe = getRecipeFromNBT(world, nbt);
        Item inputItem = getInputItemFromNBT(nbt);

        if (recipe == null) return;

        Block outputBlock = Block.byItem(recipe.getResultItem().getItem());
        int outputCnt = recipe.getResultItem().getCount();

        if(outputBlock == Blocks.AIR) return;

        BlockRayTraceResult blockRayTraceResult = getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.ANY);

        List<BlockPos> toReplace;

        if (is.getItem() instanceof EnderPortableStonecutterItem) {
            switch (((EnderPortableStonecutterItem) is.getItem()).getMode(is)) {
                case THREE_BY_THREE:
                    toReplace = toReplaceThreeByThree(world, Block.byItem(inputItem), pos, blockRayTraceResult.getDirection());
                    break;
                case LINE:
                    toReplace = toReplaceLine(world, Block.byItem(inputItem), pos, blockRayTraceResult.getDirection(), 3);
                    break;
                case NORMAL:
                default:
                    toReplace = new ArrayList<>();
                    toReplace.add(pos);
                    break;
            }
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

    private static List<BlockPos> toReplaceThreeByThree(World world, Block validBlock, BlockPos pos, Direction direction) {
        Stream<BlockPos> toReplaceStream;

        switch (direction.getAxis()) {
            case X:
                toReplaceStream = BlockPos.betweenClosedStream(pos.offset(0, -1, -1), pos.offset(0, 1, 1));
                break;
            case Y:
                toReplaceStream = BlockPos.betweenClosedStream(pos.offset(-1, 0, -1), pos.offset(1, 0, 1));
                break;
            case Z:
                toReplaceStream = BlockPos.betweenClosedStream(pos.offset(-1, -1, 0), pos.offset(1, 1, 0));
                break;
            default:
                toReplaceStream = BlockPos.betweenClosedStream(pos, pos);
                break;
        }
        return toReplaceStream.map(BlockPos::immutable)
                .filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(validBlock))
                .collect(Collectors.toList());
    }

    private static List<BlockPos> toReplaceLine(World world, Block validBlock, BlockPos pos, Direction direction, int range) {
        return BlockPos.betweenClosedStream(pos, pos.relative(direction, -range)).map(BlockPos::immutable)
                .filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(validBlock))
                .collect(Collectors.toList());
    }

    public INamedContainerProvider getContainer(World worldIn, PlayerEntity playerIn) {
        return new SimpleNamedContainerProvider((id, inventory, player) -> new PortableStonecutterContainer(id, inventory), CONTAINER_NAME);
    }
}