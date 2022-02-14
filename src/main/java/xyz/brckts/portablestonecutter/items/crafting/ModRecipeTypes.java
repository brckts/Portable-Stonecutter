package xyz.brckts.portablestonecutter.items.crafting;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.api.IAnvilFlatteningRecipe;

//TODO: FIX RECIPES
public class ModRecipeTypes {

    public static final RecipeType<IAnvilFlatteningRecipe> ANVIL_FLATTENING_TYPE = registerType(IAnvilFlatteningRecipe.TYPE_ID);
    public static final RecipeSerializer<RecipeAnvilFlattening> ANVIL_FLATTENING_RECIPE_SERIALIZER = new RecipeAnvilFlattening.Serializer();

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PortableStonecutter.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> ANVIL_FLATTENING_SERIALIZER = RECIPE_SERIALIZERS.register("anvil_flattening", RecipeAnvilFlattening.Serializer::new);

    private static <T extends Recipe<?>> RecipeType<T> registerType(ResourceLocation recipeTypeId) {
        return Registry.register(Registry.RECIPE_TYPE, recipeTypeId, new RecipeType<T>()
        {
            @Override
            public String toString() {
                return recipeTypeId.toString();
            }
        });
    }

    public static void init() {
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PortableStonecutter.LOGGER.debug("REGISTERED RECIPETYPE " + ANVIL_FLATTENING_TYPE);
    }
}
