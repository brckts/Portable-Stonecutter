package xyz.brckts.portablestonecutter.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class EnderPortableStonecutterItem extends PortableStonecutterItem {
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.ender_portable_stonecutter");

    public EnderPortableStonecutterItem() { super(); }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (playerIn.isCrouching()) {
            if (!worldIn.isRemote()) {
                playerIn.sendMessage(new StringTextComponent("Switched to " + "PENDING" + " mode"), playerIn.getUniqueID());
            }
            return ActionResult.resultSuccess(playerIn.getActiveItemStack());
        } else {
            return super.onItemRightClick(worldIn, playerIn, handIn);
        }
    }
}
