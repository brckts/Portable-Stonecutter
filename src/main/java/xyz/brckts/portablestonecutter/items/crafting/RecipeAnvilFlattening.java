package xyz.brckts.portablestonecutter.items.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.brckts.portablestonecutter.api.IAnvilFlatteningRecipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAnvilFlattening implements IAnvilFlatteningRecipe {

    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> inputs;

    public RecipeAnvilFlattening(ResourceLocation id, ItemStack output, Ingredient... inputs) {
        this.id = id;
        this.output = output;
        this.inputs = NonNullList.from(Ingredient.EMPTY, inputs);
    }

    @Override
    public boolean matches(RecipeWrapper inv, World worldIn) {
        List<Ingredient> ingredientsMissing = new ArrayList<>(inputs);

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack input = inv.getStackInSlot(i);
            if (input.isEmpty()) {
                break;
            }

            int stackIndex = -1;

            int count = input.getCount();
            while (count > 0) {
                for (int j = 0; j < ingredientsMissing.size(); j++) {
                    Ingredient ingr = ingredientsMissing.get(j);
                    if (ingr.test(input)) {
                        stackIndex = j;
                        break;
                    }
                }

                if (stackIndex != -1) {
                    ingredientsMissing.remove(stackIndex);
                    count--;
                } else {
                    return false;
                }
            }
        }

        return ingredientsMissing.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(RecipeWrapper inv) {
        return this.output;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return this.output;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ANVIL_FLATTENING_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RecipeAnvilFlattening> {

        @Override
        public RecipeAnvilFlattening read(ResourceLocation recipeId, JsonObject json) {
            ItemStack ouput = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
            JsonArray ingrs = JSONUtils.getJsonArray(json, "ingredients");
            List<Ingredient> inputs = new ArrayList<>();
            for (JsonElement e : ingrs) {
                inputs.add(Ingredient.deserialize(e));
            }
            return new RecipeAnvilFlattening(recipeId, ouput, inputs.toArray(new Ingredient[0]));
        }

        @Override
        public RecipeAnvilFlattening read(ResourceLocation recipeId, PacketBuffer buf) {
            Ingredient[] inputs = new Ingredient[buf.readVarInt()];
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = Ingredient.read(buf);
            }
            ItemStack output = buf.readItemStack();
            return new RecipeAnvilFlattening(recipeId, output, inputs);
        }

        @Override
        public void write(PacketBuffer buf, RecipeAnvilFlattening recipe) {
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient input : recipe.getIngredients()) {
                input.write(buf);
            }
            buf.writeItemStack(recipe.getRecipeOutput(), false);
        }
    }
}
