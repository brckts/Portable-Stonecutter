package xyz.brckts.portablestonecutter.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;
import xyz.brckts.portablestonecutter.network.MessageButtonPressed;
import xyz.brckts.portablestonecutter.network.MessageLockRecipe;
import xyz.brckts.portablestonecutter.network.MessageSelectRecipe;
import xyz.brckts.portablestonecutter.network.NetworkHandler;
import xyz.brckts.portablestonecutter.util.client.RenderHelper;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PortableStonecutterScreen extends AbstractContainerScreen<PortableStonecutterContainer> {

    private float sliderProgress;
    /** Is {@code true} if the player clicked on the scroll wheel in the GUI. */
    private boolean clickedOnScroll;
    private boolean clickedOnAll;
    private boolean clickedOn64;
    private int recipeIndexOffset;
    private boolean hasItemsInInputSlot;

    final static int INPUT_X = 12;
    final static int INPUT_Y = 12;
    final static int OUTPUT_X = 12;
    final static int OUTPUT_Y = 49;
    final static int SLIDER_X = 156;
    final static int SLIDER_Y = 15;
    final static int SLIDER_TEXTURE_X = 176;
    final static int SLIDER_TEXTURE_WIDTH = 12;
    final static int SLIDER_TEXTURE_HEIGHT = 15;
    final static int SLIDER_MAX_Y_OFFSET = 46;
    final static int RECIPE_AREA_X_OFFSET = 41;
    final static int RECIPE_AREA_Y_OFFSET = 14;
    final static int RECIPE_TILE_WIDTH = 16;
    final static int RECIPE_TILE_HEIGHT = 18;
    final static int BUTTON_TEXTURE_X_OFFSET = 176;
    final static int BUTTON_TEXTURE_Y_OFFSET = 15;
    final static int BUTTON_WIDTH = 13;
    final static int BUTTON_HEIGHT = 9;
    final static int LOCK_BUTTON_HEIGHT = 11;
    final static int LOCK_BUTTON_WIDTH = 14;
    final static int LOCK_BUTTON_X = 155;
    final static int LOCK_BUTTON_Y = 71;
    final static int BUTTONS_START_X = 7;
    final static int BUTTONS_START_Y = 34;
    final static int RESULTS_PER_LINE = 7;
    final static int RESULTS_MAX = 21;
    final static int LINES_SHOWN = 3;
    final static int TITLE_X = 41;
    final static int TITLE_Y = 5;

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(PortableStonecutter.MOD_ID, "textures/gui/portable_stonecutter_gui.png");

    public PortableStonecutterScreen(PortableStonecutterContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        screenContainer.setInventoryUpdateListener(this::onInventoryUpdate);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 175;
        this.imageHeight = 165;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        super.renderLabels(guiGraphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int sliderYOffset = (int)((float)SLIDER_MAX_Y_OFFSET * this.sliderProgress);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + SLIDER_X, this.topPos + SLIDER_Y + sliderYOffset, SLIDER_TEXTURE_X + (this.canScroll() ? 0 : SLIDER_TEXTURE_WIDTH), 0, SLIDER_TEXTURE_WIDTH, SLIDER_TEXTURE_HEIGHT);
        this.drawButtons(guiGraphics, mouseX, mouseY);
        int recipeAreaStartX = this.leftPos + RECIPE_AREA_X_OFFSET;
        int recipeAreaStartY = this.topPos + RECIPE_AREA_Y_OFFSET;
        int lastShownRecipeIndex = this.recipeIndexOffset + RESULTS_MAX;
        this.drawRecipeFrames(guiGraphics, mouseX, mouseY, recipeAreaStartX, recipeAreaStartY, lastShownRecipeIndex);
        this.drawRecipesItems(guiGraphics, recipeAreaStartX, recipeAreaStartY, lastShownRecipeIndex);
        if (this.menu.isRecipeLocked()) this.drawLockedItem(guiGraphics);
    }
    private void drawRecipeFrames(GuiGraphics guiGraphics, int mouseX, int mouseY, int recipeAreaStartX, int recipeAreaStartY, int lastShownRecipeIndex) {
        for(int i = this.recipeIndexOffset; i < lastShownRecipeIndex && i < this.menu.getNumRecipes(); ++i) {
            int j = i - this.recipeIndexOffset;
            int columnStartX = recipeAreaStartX + j % RESULTS_PER_LINE * RECIPE_TILE_WIDTH;
            int line = j / RESULTS_PER_LINE;
            int lineStartY = recipeAreaStartY + line * RECIPE_TILE_HEIGHT + 2;
            int tileTextureStartY = this.imageHeight;
            if (i == this.menu.getSelectedRecipeIndex()) {
                tileTextureStartY += RECIPE_TILE_HEIGHT;
            } else if (mouseX >= columnStartX && mouseY >= lineStartY && mouseX < columnStartX + RECIPE_TILE_WIDTH && mouseY < lineStartY + RECIPE_TILE_HEIGHT) {
                tileTextureStartY += RECIPE_TILE_HEIGHT * 2;
            }

            guiGraphics.blit(BACKGROUND_TEXTURE, columnStartX, lineStartY - 1, 0, tileTextureStartY+1, RECIPE_TILE_WIDTH, RECIPE_TILE_HEIGHT);
        }
    }

    private void drawButtons(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + BUTTONS_START_X, this.topPos + BUTTONS_START_Y, BUTTON_TEXTURE_X_OFFSET, BUTTON_TEXTURE_Y_OFFSET + (this.clickedOnAll ? BUTTON_HEIGHT : 0), BUTTON_WIDTH, BUTTON_HEIGHT);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + BUTTONS_START_X + BUTTON_WIDTH, this.topPos + BUTTONS_START_Y, BUTTON_TEXTURE_X_OFFSET + BUTTON_WIDTH, BUTTON_TEXTURE_Y_OFFSET + (this.clickedOn64 ? BUTTON_HEIGHT : 0), BUTTON_WIDTH, BUTTON_HEIGHT);
        if (this.menu.isLockable() || this.menu.isRecipeLocked()) {
            guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + LOCK_BUTTON_X, this.topPos + LOCK_BUTTON_Y, BUTTON_TEXTURE_X_OFFSET, BUTTON_TEXTURE_Y_OFFSET + BUTTON_HEIGHT * 2 + (this.menu.isRecipeLocked() ? LOCK_BUTTON_HEIGHT : 0), LOCK_BUTTON_WIDTH, LOCK_BUTTON_HEIGHT);
        }
    }

    protected void renderHoveredTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        if (this.hasItemsInInputSlot) {
            int i = this.leftPos + RECIPE_AREA_X_OFFSET;
            int j = this.topPos + RECIPE_AREA_Y_OFFSET;
            int k = this.recipeIndexOffset + RESULTS_MAX;
            List<RecipeHolder<StonecutterRecipe>> list = this.menu.getRecipeList();

            for(int l = this.recipeIndexOffset; l < k && l < this.menu.getNumRecipes(); ++l) {
                int i1 = l - this.recipeIndexOffset;
                int j1 = i + i1 % RESULTS_PER_LINE * RECIPE_TILE_WIDTH;
                int k1 = j + i1 / RESULTS_PER_LINE * RECIPE_TILE_HEIGHT + 2;
                if (mouseX >= j1 && mouseX < j1 + RECIPE_TILE_WIDTH && mouseY >= k1 && mouseY < k1 + RECIPE_TILE_HEIGHT) {
                    guiGraphics.renderTooltip(this.font, list.get(l).value().getResultItem(this.minecraft.level.registryAccess()), mouseX, mouseY);
                }
            }
        }
    }

    private void drawRecipesItems(GuiGraphics guiGraphics, int left, int top, int recipeIndexOffsetMax) {
        List<RecipeHolder<StonecutterRecipe>> list = this.menu.getRecipeList();

        for(int i = this.recipeIndexOffset; i < recipeIndexOffsetMax && i < this.menu.getNumRecipes(); ++i) {
            int j = i - this.recipeIndexOffset;
            int k = left + j % RESULTS_PER_LINE * RECIPE_TILE_WIDTH;
            int l = j / RESULTS_PER_LINE;
            int i1 = top + l * RECIPE_TILE_HEIGHT + 2;
            guiGraphics.renderItem(list.get(i).value().getResultItem(this.minecraft.level.registryAccess()), k, i1);
        }
    }

    private void drawLockedItem(GuiGraphics guiGraphics) {
        if (this.menu.getLockedRecipe() != null && this.menu.getLockedInput() != null) {
            RenderHelper.renderGhostItem(guiGraphics, this.minecraft, new ItemStack(this.menu.getLockedInput()), this.leftPos + INPUT_X, this.topPos + INPUT_Y);
            RenderHelper.renderGhostItem(guiGraphics, this.minecraft, this.menu.getLockedRecipe().value().getResultItem(this.minecraft.level.registryAccess()), this.leftPos + OUTPUT_X, this.topPos + OUTPUT_Y);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.clickedOnScroll = false;
        this.clickedOnAll = false;
        this.clickedOn64 = false;
        int i, j;
        if (this.hasItemsInInputSlot) {
            i = this.leftPos + RECIPE_AREA_X_OFFSET;
            j = this.topPos + RECIPE_AREA_Y_OFFSET;
            int k = this.recipeIndexOffset + RESULTS_MAX;

            for (int l = this.recipeIndexOffset; l < k; ++l) {
                int i1 = l - this.recipeIndexOffset;
                double d0 = mouseX - (double) (i + i1 % RESULTS_PER_LINE * RECIPE_TILE_WIDTH);
                double d1 = mouseY - (double) (j + i1 / RESULTS_PER_LINE * RECIPE_TILE_HEIGHT);
                if (d0 >= 0.0D && d1 >= 0.0D && d0 < 16.0D && d1 < 18.0D && this.menu.selectRecipe(l)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    PacketDistributor.SERVER.noArg().send(new MessageSelectRecipe(l));
                    return true;
                }
            }

            i = this.leftPos + SLIDER_X;
            j = this.topPos + SLIDER_Y;
            if (mouseX >= (double) i && mouseX < (double) (i + SLIDER_TEXTURE_WIDTH) && mouseY >= (double) j && mouseY < (double) (j + 54)) {
                this.clickedOnScroll = true;
            }
        }

        if (this.hasItemsInInputSlot || this.menu.isRecipeLocked()) {
            i = this.leftPos + BUTTONS_START_X;
            j = this.topPos + BUTTONS_START_Y;
            if (mouseX >= (double) i && mouseX < (double) (i + BUTTON_WIDTH) && mouseY >= (double) j && mouseY < (double) (j + BUTTON_HEIGHT)) {
                this.clickedOnAll = true;
                PacketDistributor.SERVER.noArg().send(new MessageButtonPressed(MessageButtonPressed.CRAFT_ALL_BUTTON));
            }

            i = this.leftPos + BUTTONS_START_X + BUTTON_WIDTH;
            j = this.topPos + BUTTONS_START_Y;
            if (mouseX >= (double) i && mouseX < (double) (i + BUTTON_WIDTH) && mouseY >= (double) j && mouseY < (double) (j + BUTTON_HEIGHT)) {
                this.clickedOn64 = true;
                PacketDistributor.SERVER.noArg().send(new MessageButtonPressed(MessageButtonPressed.CRAFT_64_BUTTON));
            }
        }

        i = this.leftPos + LOCK_BUTTON_X;
        j = this.topPos + LOCK_BUTTON_Y;
        if (mouseX >= (double)i && mouseX < (double)(i + LOCK_BUTTON_WIDTH) && mouseY >= (double)j && mouseY < (double)(j + LOCK_BUTTON_HEIGHT)) {
            if (this.menu.isRecipeLocked()) {
                this.menu.setRecipeLocked(false);
                PacketDistributor.SERVER.noArg().send(new MessageLockRecipe(this.menu.getSelectedRecipeIndex(), false));
            } else if (this.menu.getSelectedRecipeIndex() != -1) {
                this.menu.setRecipeLocked(true);
                PacketDistributor.SERVER.noArg().send(new MessageLockRecipe(this.menu.getSelectedRecipeIndex(), true));
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.clickedOnScroll && this.canScroll()) {
            int i = this.topPos + SLIDER_Y;
            int j = i + 54;
            this.sliderProgress = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.sliderProgress = Mth.clamp(this.sliderProgress, 0.0F, 1.0F);
            this.recipeIndexOffset = (int)((double)(this.sliderProgress * (float)this.getHiddenRows()) + 0.5D) * RESULTS_PER_LINE;
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.canScroll()) {
            int i = this.getHiddenRows();
            this.sliderProgress = (float)((double)this.sliderProgress - delta / (double)i);
            this.sliderProgress = Mth.clamp(this.sliderProgress, 0.0F, 1.0F);
            this.recipeIndexOffset = (int)((double)(this.sliderProgress * (float)i) + 0.5D) * RESULTS_PER_LINE;
        }

        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.clickedOn64 = false;
        this.clickedOnAll = false;
        this.clickedOnScroll = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean canScroll() {
        return this.hasItemsInInputSlot && this.menu.getNumRecipes() > RESULTS_MAX;
    }

    protected int getHiddenRows() {
        return (this.menu.getNumRecipes() + RESULTS_PER_LINE - 1) / RESULTS_PER_LINE - LINES_SHOWN;
    }

    /**
     * Called every time this screen's container is changed (is marked as dirty).
     */
    private void onInventoryUpdate() {
        this.hasItemsInInputSlot = this.menu.hasInputItem();
        if (!this.hasItemsInInputSlot) {
            this.sliderProgress = 0.0F;
            this.recipeIndexOffset = 0;
        }
    }
}
