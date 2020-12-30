package xyz.brckts.portablestonecutter.items.crafting;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.api.IAnvilFlatteningRecipe;

public class ModRecipeTypes {

    public static final IRecipeType<IAnvilFlatteningRecipe> ANVIL_FLATTENING_TYPE = registerType(IAnvilFlatteningRecipe.TYPE_ID);
    public static final IRecipeSerializer<RecipeAnvilFlattening> ANVIL_FLATTENING_RECIPE_SERIALIZER = new RecipeAnvilFlattening.Serializer();

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PortableStonecutter.MOD_ID);

    public static final RegistryObject<IRecipeSerializer<?>> ANVIL_FLATTENING_SERIALIZER = RECIPE_SERIALIZERS.register("anvil_flattening", () -> ANVIL_FLATTENING_RECIPE_SERIALIZER);

    private static class RecipeType<T extends IRecipe<?>> implements IRecipeType<T> {
        @Override
        public String toString() {
            return Registry.RECIPE_TYPE.getKey(this).toString();
        }
    }

    private static <T extends IRecipeType> T registerType(ResourceLocation recipeTypeId) {
        return (T) Registry.register(Registry.RECIPE_TYPE, recipeTypeId, new RecipeType<>());
    }

    public static void init() {
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
