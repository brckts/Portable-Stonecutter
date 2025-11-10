package xyz.brckts.portablestonecutter.util;

import com.google.common.base.Enums;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CodecHelper {
    public static final Codec<RecipeHolder<?>> RECIPE_HOLDER_CODEC =  RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(RecipeHolder::id),
            Recipe.CODEC.fieldOf("value").forGetter(RecipeHolder::value)
    ).apply(instance, RecipeHolder::new));

    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> enumClass) {
        return Codec.STRING.flatXmap(
                value -> Enums.getIfPresent(enumClass, value)
                        .transform(DataResult::success)
                        .or(() -> DataResult.error(() -> enumClass.getName() + "." + value + " not found.")),
                value -> DataResult.success(value.name())
        );
    }
}
