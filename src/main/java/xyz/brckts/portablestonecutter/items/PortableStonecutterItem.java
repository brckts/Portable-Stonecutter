package xyz.brckts.portablestonecutter.items;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;
import xyz.brckts.portablestonecutter.util.CodecHelper;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PortableStonecutterItem extends Item {
    public record Data(RecipeHolder<?> recipe, Holder<Item> input) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CodecHelper.RECIPE_HOLDER_CODEC.optionalFieldOf("recipe").forGetter(Data::optionalRecipe),
                RegistryFixedCodec.create(Registries.ITEM).optionalFieldOf("input").forGetter(Data::optionalInput)
        ).apply(instance, Data::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs::optional), Data::optionalRecipe,
                ByteBufCodecs.holderRegistry(Registries.ITEM).apply(ByteBufCodecs::optional), Data::optionalInput,
                Data::new
        );

        public static Data EMPTY = new Data((RecipeHolder<?>) null, null);

        public static Data get(ItemStack stack) {
            return stack.getOrDefault(RegistryHandler.PORTABLE_STONECUTTER_DATA, EMPTY);
        }

        public static void set(ItemStack stack, Data data) {
            if (EMPTY.equals(data)) {
                stack.remove(RegistryHandler.PORTABLE_STONECUTTER_DATA);
                return;
            }

            stack.set(RegistryHandler.PORTABLE_STONECUTTER_DATA, data);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Data(Optional<RecipeHolder<?>> recipe, Optional<Holder<Item>> input) {
            this(recipe.orElse(null), input.orElse(null));
        }

        public Data withRecipe(RecipeHolder<StonecutterRecipe> recipe) {
            return new Data(recipe, input);
        }

        private Optional<RecipeHolder<?>> optionalRecipe() {
            return Optional.ofNullable(recipe);
        }

        @SuppressWarnings("unchecked")
        public RecipeHolder<StonecutterRecipe> recipe(Level level) {
            return (RecipeHolder<StonecutterRecipe>) level.getRecipeManager().byKey(recipe.id())
                    .filter(r -> r.value().getType().equals(RecipeType.STONECUTTING))
                    .orElse(null);
        }

        public Data withInput(Holder<Item> input) {
            return new Data(recipe, input);
        }

        private Optional<Holder<Item>> optionalInput() {
            return Optional.ofNullable(input);
        }
    }

    private static final Component CONTAINER_NAME = Component.translatable("container.portable_stonecutter");
    public PortableStonecutterItem(Item.Properties properties) {
        super(properties.stacksTo(1).component(RegistryHandler.PORTABLE_STONECUTTER_DATA, Data.EMPTY));
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

        Data data = Data.get(is);
        RecipeHolder<StonecutterRecipe> recipe = data.recipe(world);
        Holder<Item> input = data.input();

        if (recipe == null || input == null) return;

        Block inputBlock = Block.byItem(input.value());
        Block outputBlock = Block.byItem(recipe.value().getResultItem(world.registryAccess()).getItem());
        int outputCnt = recipe.value().getResultItem(world.registryAccess()).getCount();

        if(inputBlock == Blocks.AIR || outputBlock == Blocks.AIR) return;

        BlockHitResult blockRayTraceResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);

        List<BlockPos> toReplace;

        if (is.getItem() instanceof EnderPortableStonecutterItem) {
            switch (((EnderPortableStonecutterItem) is.getItem()).getMode(is)) {
                case THREE_BY_THREE ->
                        toReplace = toReplaceThreeByThree(world, inputBlock, pos, blockRayTraceResult.getDirection());
                case LINE ->
                        toReplace = toReplaceLine(world, inputBlock, pos, blockRayTraceResult.getDirection(), 2);
                default -> {
                    toReplace = new ArrayList<>();
                    if (world.getBlockState(pos).getBlock().equals(inputBlock)) toReplace.add(pos);
                }
            }
        } else {
            toReplace = new ArrayList<>();
            if (world.getBlockState(pos).getBlock().equals(inputBlock)) toReplace.add(pos);
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