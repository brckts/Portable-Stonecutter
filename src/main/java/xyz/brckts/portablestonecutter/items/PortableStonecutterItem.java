package xyz.brckts.portablestonecutter.items;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PortableStonecutterItem extends Item {
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.portable_stonecutter");
    public PortableStonecutterItem() {
        super(new Item.Properties().group(PortableStonecutter.TAB));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(!worldIn.isRemote()) {
            playerIn.openContainer(this.getContainer(worldIn, playerIn));
            playerIn.addStat(Stats.INTERACT_WITH_STONECUTTER);
            return super.onItemRightClick(worldIn, playerIn, handIn);
        } else {
            return ActionResult.resultSuccess(playerIn.getActiveItemStack());
        }


    }

//    public INamedContainerProvider getContainer(World worldIn, PlayerEntity playerIn) {
//        return new SimpleNamedContainerProvider(new IContainerProvider() {
//            @Nullable
//            @Override
//            public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
//                return new StonecutterContainer(id, inventory, IWorldPosCallable.of(worldIn, player.getPosition()));
//            }
//        }, CONTAINER_NAME);
//    }

    public INamedContainerProvider getContainer(World worldIn, PlayerEntity playerIn) {
        return new SimpleNamedContainerProvider((id, inventory, player) -> {
            return new PortableStonecutterContainer(id, inventory);
        }, CONTAINER_NAME);
    }
}