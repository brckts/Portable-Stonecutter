package xyz.brckts.portablestonecutter.items.crafting;

import jdk.nashorn.internal.ir.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.List;

public class AnvilFlatteningCraftingManager {

    public static void craft(World world, BlockPos pos) {
        if (world.isRemote()) {
            return;
        }
        List<ItemEntity> itemEntityList = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(pos));

        int redstoneCount = 0, stonecutterCount = 0, pressurePlateCount = 0;

        for (ItemEntity ie : itemEntityList) {
            ItemStack item = ie.getItem();
            if (item.isItemEqual(new ItemStack(Items.REDSTONE))) {
                redstoneCount += item.getCount();
            } else if (item.getItem().isIn(ItemTags.WOODEN_PRESSURE_PLATES)) {
                pressurePlateCount += item.getCount();
            } else if (item.isItemEqual(new ItemStack(Items.STONECUTTER))) {
                stonecutterCount += item.getCount();
            }
        }

        if (redstoneCount >= 2 && stonecutterCount >= 1 && pressurePlateCount >= 1) {
            boolean removedPP = false;
            boolean removedSC = false;
            int RdToRemove = 2;
            for (ItemEntity ie : itemEntityList) {
                ItemStack item = ie.getItem();
                if (item.isItemEqual(new ItemStack(Items.REDSTONE)) && RdToRemove > 0) {
                    if (item.getCount() >= 2) {
                        item.setCount(item.getCount() - 2);
                    } else {
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
        }
        world.addEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get())));
    }

    private static void craft(World world, BlockPos pos, Ingredient... ingredients) {

    }
}
