package xyz.brckts.portablestonecutter.items.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.brckts.portablestonecutter.PortableStonecutter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilFlatteningCraftingManager {

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveWorldEvent event) {

        if (event.getWorld().isClientSide()) return;

        Entity entity = event.getEntity();
        if(entity instanceof FallingBlockEntity fbEntity) {
            if(fbEntity.getBlockState().getBlock() instanceof AnvilBlock) {
                craft(event.getWorld(), entity.blockPosition());
            }
        }
    }

    public static void craft(Level level, BlockPos pos) {

        if (level.isClientSide()) return;

        List<ItemEntity> itemEntityList = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos));
        SimpleContainer inv = new SimpleContainer(itemEntityList.size());

        for(ItemEntity ie : itemEntityList) {
            inv.addItem(ie.getItem());
        }

        Optional<AnvilFlatteningRecipe> recipeOptional = level.getRecipeManager().getRecipeFor(AnvilFlatteningRecipe.Type.INSTANCE, inv, level);

        if (!recipeOptional.isPresent()) return;

        AnvilFlatteningRecipe recipe = recipeOptional.get();

        if (recipe.getAllowedDim() != null && !level.dimension().location().equals(recipe.getAllowedDim())) return;

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        List<Ingredient> ingredientsMissing = new ArrayList<>(ingredients);

        for(ItemEntity ie : itemEntityList) {
            ItemStack item = ie.getItem();
            for(int i = 0; i < ingredientsMissing.size(); i++) {
                Ingredient ingr = ingredientsMissing.get(i);
                if(ingr.test(item)) {
                    item.setCount(0);
                    ingredientsMissing.remove(i);
                    break;
                }
            }
        }

        level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), recipe.getResultItem()));
    }
}
