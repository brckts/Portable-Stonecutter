package xyz.brckts.portablestonecutter.items.crafting;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.api.IAnvilRecipe;

public class ModRecipeTypes {

    public static final IRecipeType<IAnvilRecipe> ANVIL_FLATTENING_TYPE = new RecipeType<>();
    public static final IRecipeSerializer<RecipeAnvilFlattening> ANVIL_FLATTENING_SERIALIZER = new RecipeAnvilFlattening.Serializer();

    public static void register(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        ResourceLocation id = new ResourceLocation(PortableStonecutter.MOD_ID, "anvil_flattening");
        Registry.register(Registry.RECIPE_TYPE, id, ANVIL_FLATTENING_TYPE);
        event.getRegistry().register(ANVIL_FLATTENING_SERIALIZER.setRegistryName(id));
    }

    private static class RecipeType<T extends IRecipe<?>> implements IRecipeType<T> {
        @Override
        public String toString() {
            return Registry.RECIPE_TYPE.getKey(this).toString();
        }
    }
}
