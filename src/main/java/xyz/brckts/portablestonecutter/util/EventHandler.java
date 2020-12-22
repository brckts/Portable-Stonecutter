package xyz.brckts.portablestonecutter.util;

import net.minecraft.block.AnvilBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.items.PortableStonecutterItem;

import java.util.List;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void onFallingAnvilRemoval(EntityLeaveWorldEvent event) {
        if(event.getWorld().isRemote()) {
            return;
        }

        Entity entity = event.getEntity();
        if(entity instanceof FallingBlockEntity) {
            FallingBlockEntity fbEntity = (FallingBlockEntity) entity;
            if(fbEntity.getBlockState().getBlock() instanceof AnvilBlock) {
                PortableStonecutterItem.craftPortableStonecutter(event.getWorld(), entity.getPosition());
            }
        }
    }
}
