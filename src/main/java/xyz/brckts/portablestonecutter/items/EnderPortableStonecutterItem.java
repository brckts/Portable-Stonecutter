package xyz.brckts.portablestonecutter.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;
import xyz.brckts.portablestonecutter.util.CodecHelper;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.Objects;

public class EnderPortableStonecutterItem extends PortableStonecutterItem {

    public enum Mode {
        NORMAL,
        THREE_BY_THREE,
        LINE;

        public static final Codec<Mode> CODEC = CodecHelper.enumCodec(Mode.class);
        public static final StreamCodec<FriendlyByteBuf, Mode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Mode.class);
    }

    public record Data(Mode mode) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Mode.CODEC.fieldOf("mode").forGetter(Data::mode)
        ).apply(instance, Data::new));

        public static final StreamCodec<FriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                Mode.STREAM_CODEC, Data::mode,
                Data::new
        );

        public static Data EMPTY = new Data(Mode.NORMAL);

        public static Data get(ItemStack stack) {
            return stack.getOrDefault(RegistryHandler.ENDER_PORTABLE_STONECUTTER_DATA, EMPTY);
        }

        public static void set(ItemStack stack, Data data) {
            if (EMPTY.equals(data)) {
                stack.remove(RegistryHandler.ENDER_PORTABLE_STONECUTTER_DATA);
                return;
            }

            stack.set(RegistryHandler.ENDER_PORTABLE_STONECUTTER_DATA, data);
        }

        public Data withMode(Mode mode) {
            return new Data(mode);
        }
    }

    private static final Component CONTAINER_NAME = Component.translatable("container.ender_portable_stonecutter");

    public EnderPortableStonecutterItem(Item.Properties properties) {
        super(properties.component(RegistryHandler.ENDER_PORTABLE_STONECUTTER_DATA, Data.EMPTY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getMainHandItem();

        if (playerIn.isCrouching()) {
            nextEPSCMode(stack);
            playerIn.displayClientMessage(Component.translatable("info.portable_stonecutter.epsc.mode." + getMode(stack).name().toLowerCase()).withStyle(ChatFormatting.DARK_GREEN), true);
            return InteractionResultHolder.success(stack);
        }

        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public MenuProvider getContainer(Level worldIn, Player playerIn) {
        return new SimpleMenuProvider((id, inventory, player) -> new PortableStonecutterContainer(id, inventory), CONTAINER_NAME);
    }

    private void nextEPSCMode(ItemStack stack) {
        Data data = Data.get(stack);
        data = data.withMode(Mode.values()[(data.mode().ordinal() + 1) % Mode.values().length]);
        Data.set(stack, data);
    }

    public Mode getMode(ItemStack stack) {
        return Data.get(stack).mode();
    }
}
