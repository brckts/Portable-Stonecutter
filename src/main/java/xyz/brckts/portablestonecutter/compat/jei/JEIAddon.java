package xyz.brckts.portablestonecutter.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        registration.addRecipes(Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ANVIL_FLATTENING_TYPE), AnvilFlatteningRecipeCategory.UID);
        registration.addIngredientInfo(new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get()), VanillaTypes.ITEM, new TranslatableComponent("info." + PortableStonecutter.MOD_ID + ":portable_stonecutter"));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AnvilFlatteningRecipeCategory((guiHelper)));
    }
}
