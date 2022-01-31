package xyz.brckts.portablestonecutter.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

public class EnderPortableStonecutterItem extends PortableStonecutterItem {

    public enum Mode {
        NORMAL,
        THREE_BY_THREE,
        LINE;
    }
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.ender_portable_stonecutter");

    public EnderPortableStonecutterItem() {
        super();
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getMainHandItem();

        if (playerIn.isCrouching()) {
            nextEPSCMode(stack);
            playerIn.displayClientMessage(new TranslationTextComponent("info.portable_stonecutter.epsc.mode." + getMode(stack).name().toLowerCase(), TextFormatting.RED).withStyle(TextFormatting.DARK_GREEN), true);
            return ActionResult.success(stack);
        }

        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public INamedContainerProvider getContainer(World worldIn, PlayerEntity playerIn) {
        return new SimpleNamedContainerProvider((id, inventory, player) -> new PortableStonecutterContainer(id, inventory), CONTAINER_NAME);
    }

    private void nextEPSCMode(ItemStack stack) {
        CompoundNBT nbt = getNBT(stack);
        int i = nbt.getInt("Mode") + 1;
        int j = Mode.values().length - 1;
        nbt.putInt("Mode", i > j ? 0 : i);
    }

    public Mode getMode(ItemStack stack) {
        return Mode.values()[getNBT(stack).getInt("Mode")];
    }

    public CompoundNBT getNBT(ItemStack stack) {
        return stack.getOrCreateTagElement("EnderPSCNBT");
    }
}
