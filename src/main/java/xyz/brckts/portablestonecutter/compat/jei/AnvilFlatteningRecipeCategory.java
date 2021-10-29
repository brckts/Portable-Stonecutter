package xyz.brckts.portablestonecutter.compat.jei;

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
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.api.IAnvilFlatteningRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnvilFlatteningRecipeCategory implements IRecipeCategory<IAnvilFlatteningRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(PortableStonecutter.MOD_ID, "anvil_flattening");
    private static final ResourceLocation texture = new ResourceLocation(PortableStonecutter.MOD_ID, "textures/gui/jei_anvil_flattening.png");

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final String title;
    private final IDrawableStatic overlay;

    public AnvilFlatteningRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(90, 90);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.ANVIL));
        this.title = I18n.get("jei." + UID.toString());
        this.overlay = guiHelper.createDrawable(texture, 0, 0, 64, 64);
    }

    @Nonnull
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends IAnvilFlatteningRecipe> getRecipeClass() {
        return IAnvilFlatteningRecipe.class;
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
    public void setIngredients(IAnvilFlatteningRecipe recipe, IIngredients ingredients) {
        List<List<ItemStack>> list = new ArrayList<>();
        for (Ingredient ingr : recipe.getIngredients()) {
            list.add(Arrays.asList(ingr.getItems()));
        }
        ingredients.setInputLists(VanillaTypes.ITEM, list);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IAnvilFlatteningRecipe recipe, IIngredients ingredients) {
        int width = 80;
        int index = 1;
        int ingrCnt = ingredients.getInputs(VanillaTypes.ITEM).size();
        int spaceBetweenEach = width / ingrCnt;
        int offset = 35 - ((ingrCnt-1) * spaceBetweenEach)/2;
        recipeLayout.getItemStacks().init(0, true, 35, 0); // anvil
        recipeLayout.getItemStacks().set(0, new ItemStack(Blocks.ANVIL));

        for (List<ItemStack> o : ingredients.getInputs(VanillaTypes.ITEM)) {
            recipeLayout.getItemStacks().init(index, true, offset + spaceBetweenEach * (index - 1), 45);
            recipeLayout.getItemStacks().set(index, o);
            index++;
        }
    }

    @Override
    public void draw(IAnvilFlatteningRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        overlay.draw(matrixStack, 13, 20);
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
    }
}
