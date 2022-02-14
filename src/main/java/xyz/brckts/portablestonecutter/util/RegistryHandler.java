package xyz.brckts.portablestonecutter.util;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;
import xyz.brckts.portablestonecutter.items.EnderPortableStonecutterItem;
import xyz.brckts.portablestonecutter.items.PortableStonecutterItem;

public class RegistryHandler {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PortableStonecutter.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, PortableStonecutter.MOD_ID);

    public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<Item> PORTABLE_STONECUTTER = ITEMS.register("portable_stonecutter", PortableStonecutterItem::new);
    public static final RegistryObject<Item> ENDER_PORTABLE_STONECUTTER = ITEMS.register("ender_portable_stonecutter", EnderPortableStonecutterItem::new);
    public static final RegistryObject<MenuType<PortableStonecutterContainer>> PORTABLE_STONECUTTER_CONTAINER = MENU_TYPES.register("portable_stonecutter_container", () -> new MenuType<>(PortableStonecutterContainer::new));
}
