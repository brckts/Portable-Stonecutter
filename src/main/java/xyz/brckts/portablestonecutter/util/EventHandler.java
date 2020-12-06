package xyz.brckts.portablestonecutter.util;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.entity.item.ItemEntity;
import xyz.brckts.portablestonecutter.PortableStonecutter;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    //TODO: find a less dirty way of doing it
    @SubscribeEvent
    public static void onAnvilFall(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        BlockState block = event.getPlacedBlock();
        BlockPos pos = event.getBlockSnapshot().getPos();
        BlockPos under = pos.add(0,-1,0);
        if(entity instanceof ServerPlayerEntity
                && block.getBlock() instanceof AnvilBlock
                && event.getWorld().isAirBlock(under)) {
            List<ItemEntity> itemEntityList = event.getWorld().getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(under));

            int redstoneCount = 0, stonecutterCount = 0, pressurePlateCount = 0;

            for(ItemEntity ie : itemEntityList) {
                ItemStack item = ie.getItem();
                if(item.isItemEqual(new ItemStack(Items.REDSTONE))) {
                    redstoneCount += item.getCount();
                } else if (item.getItem().isIn(ItemTags.WOODEN_PRESSURE_PLATES)) {
                    pressurePlateCount += item.getCount();
                } else if (item.isItemEqual(new ItemStack(Items.STONECUTTER))) {
                    stonecutterCount += item.getCount();
                }
            }

            if (redstoneCount >= 2 && stonecutterCount >=1 && pressurePlateCount >= 1) {
                boolean removedPP = false;
                boolean removedSC = false;
                int RdToRemove = 2;
                for(ItemEntity ie : itemEntityList) {
                    ItemStack item = ie.getItem();
                    if(item.isItemEqual(new ItemStack(Items.REDSTONE)) && RdToRemove > 0) {
                        if(item.getCount() >= 2) { item.setCount(item.getCount() - 2); }
                        else {
                            RdToRemove -= item.getCount();
                            item.setCount(0);
                        }
                    } else if (item.getItem().isIn(ItemTags.WOODEN_PRESSURE_PLATES) && !removedPP) {
                        item.setCount(item.getCount() - 1);
                        removedPP = true;
                    } else if (item.isItemEqual(new ItemStack(Items.STONECUTTER)) && !removedSC) {
                        item.setCount(item.getCount() - 1);
                        removedSC = true;
                    }
                }

                //TODO: Learn how to do sounds and particles
            }

        }
    }
}
