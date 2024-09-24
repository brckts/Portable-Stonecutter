package xyz.brckts.portablestonecutter.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.items.crafting.AnvilFlatteningRecipe;

@Mod.EventBusSubscriber(modid = PortableStonecutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEventHandler {
    @SubscribeEvent
    public static void registerRecipeTypes(final RegisterEvent event) {
        ForgeRegistries.RECIPE_TYPES.register(AnvilFlatteningRecipe.Type.ID, AnvilFlatteningRecipe.Type.INSTANCE);
    }

    @SubscribeEvent
    public static void registerCreativeTabs(final CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(PortableStonecutter.MOD_ID, "tab"), builder -> builder
                .title(Component.translatable("itemGroup.portableStonecutter"))
                .icon(() -> new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get()))
                .displayItems((parameters, output) -> {
                    output.accept(RegistryHandler.PORTABLE_STONECUTTER.get());
                    output.accept(RegistryHandler.ENDER_PORTABLE_STONECUTTER.get());
                })
        );
    }
}
