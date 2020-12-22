package xyz.brckts.portablestonecutter.util;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;
import xyz.brckts.portablestonecutter.items.PortableStonecutterItem;
import xyz.brckts.portablestonecutter.items.crafting.RecipeAnvilFlattening;

public class RegistryHandler {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PortableStonecutter.MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, PortableStonecutter.MOD_ID);

    public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINER_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<Item> PORTABLE_STONECUTTER = ITEMS.register("portable_stonecutter", PortableStonecutterItem::new);
    public static final RegistryObject<ContainerType<PortableStonecutterContainer>> PORTABLE_STONECUTTER_CONTAINER = CONTAINER_TYPES.register("portable_stonecutter_container", () -> IForgeContainerType.create(PortableStonecutterContainer::new));
}
