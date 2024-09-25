package xyz.brckts.portablestonecutter.items;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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
    private static final Component CONTAINER_NAME = Component.translatable("container.portable_stonecutter");
    public PortableStonecutterItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if(!worldIn.isClientSide()) {
            //clearTags(playerIn);
            playerIn.openMenu(this.getContainer(worldIn, playerIn));
            playerIn.awardStat(Stats.INTERACT_WITH_STONECUTTER);
            return super.use(worldIn, playerIn, handIn);
        } else {
            return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        if(world.isClientSide()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();

        ItemStack is = event.getItemStack();

        if(!(is.getItem() instanceof PortableStonecutterItem)) {
            return;
        }

        CompoundTag nbt = is.getTag();
        RecipeHolder<StonecutterRecipe> recipe = getRecipeFromNBT(world, nbt);
        Item inputItem = getInputItemFromNBT(nbt);

        if (recipe == null) return;

        Block outputBlock = Block.byItem(recipe.value().getResultItem(world.registryAccess()).getItem());
        int outputCnt = recipe.value().getResultItem(world.registryAccess()).getCount();

        if(outputBlock == Blocks.AIR) return;

        BlockHitResult blockRayTraceResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);

        List<BlockPos> toReplace;

        if (is.getItem() instanceof EnderPortableStonecutterItem) {
            switch (((EnderPortableStonecutterItem) is.getItem()).getMode(is)) {
                case THREE_BY_THREE ->
                        toReplace = toReplaceThreeByThree(world, Block.byItem(inputItem), pos, blockRayTraceResult.getDirection());
                case LINE ->
                        toReplace = toReplaceLine(world, Block.byItem(inputItem), pos, blockRayTraceResult.getDirection(), 2);
                default -> {
                    toReplace = new ArrayList<>();
                    if (world.getBlockState(pos).getBlock().equals(Block.byItem(inputItem))) toReplace.add(pos);
                }
            }
        } else {
            toReplace = new ArrayList<>();
            if (world.getBlockState(pos).getBlock().equals(Block.byItem(inputItem))) toReplace.add(pos);
        }

        BlockState outputState = outputBlock.getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, blockRayTraceResult)));

        for (BlockPos blockPos : toReplace) {
            BlockState inputState = world.getBlockState(blockPos);
            world.setBlockAndUpdate(blockPos, outputState);
            world.levelEvent(2001, blockPos, Block.getId(inputState));

            if (outputCnt > 1) {
                player.drop(new ItemStack(recipe.value().getResultItem(player.level().registryAccess()).getItem(), outputCnt - 1), true, true);
            }
        }
    }

    private static List<BlockPos> toReplaceThreeByThree(Level world, Block validBlock, BlockPos pos, Direction direction) {
        Stream<BlockPos> toReplaceStream = switch (direction.getAxis()) {
            case X -> BlockPos.betweenClosedStream(pos.offset(0, -1, -1), pos.offset(0, 1, 1));
            case Y -> BlockPos.betweenClosedStream(pos.offset(-1, 0, -1), pos.offset(1, 0, 1));
            case Z -> BlockPos.betweenClosedStream(pos.offset(-1, -1, 0), pos.offset(1, 1, 0));
            default -> BlockPos.betweenClosedStream(pos, pos);
        };

        return toReplaceStream.map(BlockPos::immutable)
                .filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(validBlock))
                .collect(Collectors.toList());
    }

    private static List<BlockPos> toReplaceLine(Level world, Block validBlock, BlockPos pos, Direction direction, int range) {
        return BlockPos.betweenClosedStream(pos, pos.relative(direction, -range)).map(BlockPos::immutable)
                .filter(blockPos -> world.getBlockState(blockPos).getBlock().equals(validBlock))
                .collect(Collectors.toList());
    }

    public MenuProvider getContainer(Level worldIn, Player playerIn) {
        return new SimpleMenuProvider((id, inventory, player) -> new PortableStonecutterContainer(id, inventory), CONTAINER_NAME);
    }
}