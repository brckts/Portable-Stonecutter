package xyz.brckts.portablestonecutter.compat.jei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

import javax.annotation.Nonnull;

public class AnvilFlatteningRecipeCategory implements IRecipeCategory<AnvilFlatteningRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(PortableStonecutter.MOD_ID, "anvil_flattening");
    private static final ResourceLocation texture = new ResourceLocation(PortableStonecutter.MOD_ID, "textures/gui/jei_anvil_flattening.png");

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final String title;
    private final IDrawableStatic overlay;

    public AnvilFlatteningRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(90, 90);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.ANVIL));
        this.title = I18n.format("jei." + UID.toString());
        this.overlay = guiHelper.createDrawable(texture, 0, 0, 64, 64);
    }

    @Nonnull
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends AnvilFlatteningRecipe> getRecipeClass() {
        return AnvilFlatteningRecipe.class;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return title;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(AnvilFlatteningRecipe anvilFlatteningRecipe, IIngredients iIngredients) {
        //iIngredients.setInputs(VanillaTypes.ITEM, ImmutableList.of(new ItemStack(Items.ANVIL), new ItemStack(Items.OAK_PRESSURE_PLATE), new ItemStack(Items.REDSTONE, 2), new ItemStack(Items.STONECUTTER)));
        iIngredients.setInputIngredients(ImmutableList.of(Ingredient.fromItems(Items.ANVIL), Ingredient.fromTag(ItemTags.WOODEN_PRESSURE_PLATES), Ingredient.fromStacks(new ItemStack(Items.REDSTONE, 2)), Ingredient.fromItems(Items.STONECUTTER)));
        iIngredients.setOutput(VanillaTypes.ITEM, new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get()));

    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, AnvilFlatteningRecipe anvilFlatteningRecipe, IIngredients iIngredients) {
        iRecipeLayout.getItemStacks().init(0, true, 35, 0);
        iRecipeLayout.getItemStacks().init(1, true, 12, 42);
        iRecipeLayout.getItemStacks().init(2, true, 60, 42);
        iRecipeLayout.getItemStacks().init(3, true, 35, 42);
        iRecipeLayout.getItemStacks().set(iIngredients);
    }

    @Override
    public void draw(AnvilFlatteningRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        overlay.draw(matrixStack, 13, 20);
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
    }
}
