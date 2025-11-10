package xyz.brckts.portablestonecutter.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

public class EnderPortableStonecutterItem extends PortableStonecutterItem {

    public enum Mode {
        NORMAL,
        THREE_BY_THREE,
        LINE
    }
    private static final Component CONTAINER_NAME = new TranslatableComponent("container.ender_portable_stonecutter");

    public EnderPortableStonecutterItem() {
        super();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getMainHandItem();

        if (playerIn.isCrouching()) {
            nextEPSCMode(stack);
            playerIn.displayClientMessage(new TranslatableComponent("info.portable_stonecutter.epsc.mode." + getMode(stack).name().toLowerCase(), ChatFormatting.RED).withStyle(ChatFormatting.DARK_GREEN), true);
            return InteractionResultHolder.success(stack);
        }

        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public MenuProvider getContainer(Level worldIn, Player playerIn) {
        return new SimpleMenuProvider((id, inventory, player) -> new PortableStonecutterContainer(id, inventory), CONTAINER_NAME);
    }

    private void nextEPSCMode(ItemStack stack) {
        CompoundTag nbt = getNBT(stack);
        int i = nbt.getInt("Mode") + 1;
        int j = Mode.values().length - 1;
        nbt.putInt("Mode", i > j ? 0 : i);
    }

    public Mode getMode(ItemStack stack) {
        return Mode.values()[getNBT(stack).getInt("Mode")];
    }

    public CompoundTag getNBT(ItemStack stack) {
        return stack.getOrCreateTagElement("EnderPSCNBT");
    }
}
