package xyz.brckts.portablestonecutter.items;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;


public class PortableStonecutterItem extends Item {
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.portable_stonecutter");
    public PortableStonecutterItem() {
        super(new Item.Properties().group(PortableStonecutter.TAB).maxStackSize(1));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(!worldIn.isRemote()) {
            playerIn.getHeldItemMainhand().setTag(null);
            playerIn.openContainer(this.getContainer(worldIn, playerIn));
            playerIn.addStat(Stats.INTERACT_WITH_STONECUTTER);
            return super.onItemRightClick(worldIn, playerIn, handIn);
        } else {
            return ActionResult.resultSuccess(playerIn.getActiveItemStack());
        }
    }

    public INamedContainerProvider getContainer(World worldIn, PlayerEntity playerIn) {
        return new SimpleNamedContainerProvider((id, inventory, player) -> {
            return new PortableStonecutterContainer(id, inventory);
        }, CONTAINER_NAME);
    }
}