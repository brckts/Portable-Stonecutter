package xyz.brckts.portablestonecutter.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.items.crafting.AnvilFlatteningCraftingManager;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void onFallingAnvilRemoval(EntityLeaveWorldEvent event) {
        if(event.getWorld().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();
        if(entity instanceof FallingBlockEntity) {
            FallingBlockEntity fbEntity = (FallingBlockEntity) entity;
            if(fbEntity.getBlockState().getBlock() instanceof AnvilBlock) {
                AnvilFlatteningCraftingManager.craft(event.getWorld(), entity.blockPosition());
            }
        }
    }
}
