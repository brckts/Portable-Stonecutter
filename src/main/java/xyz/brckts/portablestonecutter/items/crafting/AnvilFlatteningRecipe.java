package xyz.brckts.portablestonecutter.items.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnvilFlatteningRecipe implements Recipe<SimpleContainer> {

    final ItemStack output;
    private final NonNullList<Ingredient> inputs;
    //private final ResourceLocation allowedDim;
    private final ResourceLocation allowedDim;

    public AnvilFlatteningRecipe(ResourceLocation allowedDim, ItemStack output, NonNullList<Ingredient> inputs) {
        this.output = output;
        this.inputs = inputs;
        this.allowedDim = allowedDim;
    }

    @Override
    public boolean matches(SimpleContainer inv, Level pLevel) {

        List<Ingredient> ingredientsMissing = new ArrayList<>(this.inputs);

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack input = inv.getItem(i);
            if (input.isEmpty()) {
                break;
            }

            int count = input.getCount();
            while (count > 0) {

                int stackIndex = -1;
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
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess registryAccess) {
        return this.output;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return this.output.copy();
    }

    public ResourceLocation getAllowedDim() {
        return this.allowedDim;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryHandler.ANVIL_FLATTENING_RECIPE_TYPE.get();
    }

    public static class Type implements RecipeType<AnvilFlatteningRecipe> {
        public Type() {}
        public static final String ID = "anvil_flattening";
    }

    public static class Serializer implements RecipeSerializer<AnvilFlatteningRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(PortableStonecutter.MOD_ID, "anvil_flattening");
        @Override
        public Codec<AnvilFlatteningRecipe> codec() {
            return RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.optionalFieldOf("allowed_dim").forGetter(r -> Optional.ofNullable(r.getAllowedDim())),
                            ItemStack.RESULT_CODEC.fieldOf("output").forGetter(r -> r.output),
                            NonNullList.codecOf(Ingredient.CODEC_NONEMPTY).fieldOf("ingredients").forGetter(AnvilFlatteningRecipe::getIngredients)
                    ).apply(instance, (allowedDim, output, ingredients) -> new AnvilFlatteningRecipe(allowedDim.orElse(null), output, ingredients)));
        }

        @Override
        public AnvilFlatteningRecipe fromNetwork(FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readInt(), Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(buf));
            }

            ItemStack output = buf.readItem();
            ResourceLocation allowedDim = null;

            if (buf.readBoolean()) {
                allowedDim = buf.readResourceLocation();
            }

            return new AnvilFlatteningRecipe(allowedDim, output, inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AnvilFlatteningRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient input : recipe.getIngredients()) {
                input.toNetwork(buf);
            }

            buf.writeItem(recipe.output);
            if (recipe.getAllowedDim() != null) {
                buf.writeBoolean(true);
                buf.writeResourceLocation(recipe.getAllowedDim());
            } else {
                buf.writeBoolean(false);
            }
        }
    }
}
