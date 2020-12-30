package xyz.brckts.portablestonecutter.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.items.crafting.ModRecipeTypes;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

@JeiPlugin
public class JEIAddon implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(PortableStonecutter.MOD_ID, "jei");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.ANVIL), AnvilFlatteningRecipeCategory.UID);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(Minecraft.getInstance().world.getRecipeManager().getRecipesForType(ModRecipeTypes.ANVIL_FLATTENING_TYPE), AnvilFlatteningRecipeCategory.UID);
        registration.addIngredientInfo(new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get()), VanillaTypes.ITEM, I18n.format("info." + PortableStonecutter.MOD_ID + ":portable_stonecutter"));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AnvilFlatteningRecipeCategory((guiHelper)));
    }
}
