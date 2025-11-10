package xyz.brckts.portablestonecutter.util;

import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.items.crafting.AnvilFlatteningRecipe;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEventHandler {
    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
        Registry.register(Registry.RECIPE_TYPE, AnvilFlatteningRecipe.Type.ID, AnvilFlatteningRecipe.Type.INSTANCE);
    }
}
