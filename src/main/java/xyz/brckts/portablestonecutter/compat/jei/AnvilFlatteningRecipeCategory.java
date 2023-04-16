package xyz.brckts.portablestonecutter.compat.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.items.crafting.AnvilFlatteningRecipe;

import javax.annotation.Nonnull;

public class AnvilFlatteningRecipeCategory implements IRecipeCategory<AnvilFlatteningRecipe> {

    public static final RecipeType<AnvilFlatteningRecipe> RECIPE_TYPE =
            RecipeType.create(PortableStonecutter.MOD_ID, "anvil_flattening", AnvilFlatteningRecipe.class);


    private static final ResourceLocation texture = new ResourceLocation(PortableStonecutter.MOD_ID, "textures/gui/jei_anvil_flattening.png");

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final Component title;
    private final IDrawableStatic overlay;

    public AnvilFlatteningRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(90, 90);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.ANVIL));
        this.title = new TranslatableComponent("jei." + RECIPE_TYPE.getUid());
        this.overlay = guiHelper.createDrawable(texture, 0, 0, 64, 64);
    }

    @Override
    public RecipeType<AnvilFlatteningRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Nonnull
    @Override
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, AnvilFlatteningRecipe recipe, IFocusGroup focuses) {

        int width = 80;
        int index = 1;
        int ingrCnt = recipe.getIngredients().size();
        int spaceBetweenEach = width / ingrCnt;
        int offset = 35 - ((ingrCnt-1) * spaceBetweenEach)/2;
        builder.addSlot(RecipeIngredientRole.CATALYST, 35, 0).addItemStack(new ItemStack(Items.ANVIL));

        for (Ingredient i : recipe.getIngredients()) {
            builder.addSlot(RecipeIngredientRole.INPUT, offset + spaceBetweenEach * (index - 1), 45).addIngredients(i);
            index++;
        }

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(recipe.getResultItem());
    }


    @Override
    public void draw(AnvilFlatteningRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        overlay.draw(stack, 13, 20);
        RenderSystem.disableBlend();
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return getRecipeType().getUid();
    }


    @SuppressWarnings("removal")
    @Override
    public Class<? extends AnvilFlatteningRecipe> getRecipeClass() {
        return getRecipeType().getRecipeClass();
    }
}
