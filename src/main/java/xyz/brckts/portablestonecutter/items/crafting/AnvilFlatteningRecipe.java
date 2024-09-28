package xyz.brckts.portablestonecutter.items.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import xyz.brckts.portablestonecutter.items.EnderPortableStonecutterItem;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnvilFlatteningRecipe implements Recipe<AnvilFlatteningInput> {

    final ItemStack output;
    private final NonNullList<Ingredient> inputs;
    //private final ResourceLocation allowedDim;
    private final ResourceLocation allowedDim;

    public AnvilFlatteningRecipe(Optional<ResourceLocation> allowedDim, ItemStack output, NonNullList<Ingredient> inputs) {
        this(allowedDim.orElse(null), output, inputs);
    }

    public AnvilFlatteningRecipe(ResourceLocation allowedDim, ItemStack output, NonNullList<Ingredient> inputs) {
        this.output = output;
        this.inputs = inputs;
        this.allowedDim = allowedDim;
    }

    @Override
    public boolean matches(AnvilFlatteningInput inv, Level pLevel) {

        List<Ingredient> ingredientsMissing = new ArrayList<>(this.inputs);

        for (int i = 0; i < inv.size(); i++) {
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
    public ItemStack assemble(AnvilFlatteningInput pContainer, HolderLookup.Provider pRegistries) {
        return this.output;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return this.output.copy();
    }

    public ResourceLocation getAllowedDim() {
        return this.allowedDim;
    }

    private Optional<ResourceLocation> getAllowedDimOptional() {
        return Optional.ofNullable(this.allowedDim);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryHandler.ANVIL_FLATTENING_SERIALIZER.get();
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
        private static final MapCodec<AnvilFlatteningRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        ResourceLocation.CODEC.optionalFieldOf("allowed_dim").forGetter(AnvilFlatteningRecipe::getAllowedDimOptional),
                        ItemStack.STRICT_CODEC.fieldOf("output").forGetter(r -> r.output),
                        NonNullList.codecOf(Ingredient.CODEC_NONEMPTY).fieldOf("ingredients").forGetter(AnvilFlatteningRecipe::getIngredients)
                ).apply(instance, (allowedDim, output, inputs) -> new AnvilFlatteningRecipe(allowedDim.orElse(null), output, inputs)));

        private static final StreamCodec<RegistryFriendlyByteBuf, AnvilFlatteningRecipe> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), AnvilFlatteningRecipe::getAllowedDimOptional,
                ItemStack.STREAM_CODEC, r -> r.output,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(size -> NonNullList.withSize(size, Ingredient.EMPTY))), AnvilFlatteningRecipe::getIngredients,
                AnvilFlatteningRecipe::new
        );

        @Override
        public MapCodec<AnvilFlatteningRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AnvilFlatteningRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
