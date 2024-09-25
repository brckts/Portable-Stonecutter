package xyz.brckts.portablestonecutter.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;
import xyz.brckts.portablestonecutter.items.EnderPortableStonecutterItem;
import xyz.brckts.portablestonecutter.items.PortableStonecutterItem;
import xyz.brckts.portablestonecutter.items.crafting.AnvilFlatteningRecipe;

public class RegistryHandler {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, PortableStonecutter.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, PortableStonecutter.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, PortableStonecutter.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), PortableStonecutter.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, PortableStonecutter.MOD_ID);

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
        MENU_TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }

    public static final DeferredHolder<Item, Item> PORTABLE_STONECUTTER = ITEMS.register("portable_stonecutter", PortableStonecutterItem::new);
    public static final DeferredHolder<Item, Item> ENDER_PORTABLE_STONECUTTER = ITEMS.register("ender_portable_stonecutter", EnderPortableStonecutterItem::new);
    public static final DeferredHolder<MenuType<?>, MenuType<PortableStonecutterContainer>> PORTABLE_STONECUTTER_CONTAINER =
            MENU_TYPES.register("portable_stonecutter_container", () -> new MenuType<>(PortableStonecutterContainer::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AnvilFlatteningRecipe>> ANVIL_FLATTENING_SERIALIZER =
            SERIALIZERS.register("anvil_flattening", () -> AnvilFlatteningRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeType<?>, AnvilFlatteningRecipe.Type> ANVIL_FLATTENING_RECIPE_TYPE = RECIPE_TYPES.register(AnvilFlatteningRecipe.Type.ID, AnvilFlatteningRecipe.Type::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PORTABLE_STONECUTTER_CREATIVE_MODE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.portableStonecutter"))
            .icon(() -> new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get()))
            .displayItems((parameters, output) -> {
                output.accept(RegistryHandler.PORTABLE_STONECUTTER.get());
                output.accept(RegistryHandler.ENDER_PORTABLE_STONECUTTER.get());
            })
            .build());
}
