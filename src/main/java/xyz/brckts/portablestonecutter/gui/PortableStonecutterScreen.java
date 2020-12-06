package xyz.brckts.portablestonecutter.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PortableStonecutterScreen extends ContainerScreen<PortableStonecutterContainer> {

    private float sliderProgress;
    /** Is {@code true} if the player clicked on the scroll wheel in the GUI. */
    private boolean clickedOnSroll;
    private boolean clickedOnAll;
    private boolean clickedOn64;
    private int recipeIndexOffset;
    private boolean hasItemsInInputSlot;

    final static int SLIDER_X = 156;
    final static int SLIDER_Y = 8;
    final static int SLIDER_TEXTURE_X = 176;
    final static int SLIDER_TEXTURE_WIDTH = 12;
    final static int SLIDER_TEXTURE_HEIGHT = 15;
    final static int SLIDER_MAX_Y_OFFSET = 46;
    final static int RECIPE_AREA_X_OFFSET = 41;
    final static int RECIPE_AREA_Y_OFFSET = 7;
    final static int RECIPE_TILE_WIDTH = 16;
    final static int RECIPE_TILE_HEIGHT = 18;
    final static int BUTTON_TEXTURE_X_OFFSET = 176;
    final static int BUTTON_TEXTURE_Y_OFFSET = 15;
    final static int BUTTON_WIDTH = 13;
    final static int BUTTON_HEIGHT = 9;
    final static int BUTTONS_START_X = 7;
    final static int BUTTONS_START_Y = 34;

    private int resultsPerLine = 7;
    private int resultsMax = 21;

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(PortableStonecutter.MOD_ID, "textures/gui/portable_stonecutter_gui.png");

    public PortableStonecutterScreen(PortableStonecutterContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        screenContainer.setInventoryUpdateListener(this::onInventoryUpdate);
        this.guiLeft = 0;
        this.guiTop = 0;
        this.xSize = 175;
        this.ySize = 165;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(matrixStack);
        this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize);
        int sliderYOffset = (int)((float)SLIDER_MAX_Y_OFFSET * this.sliderProgress);
        this.blit(matrixStack, this.guiLeft + SLIDER_X, this.guiTop + SLIDER_Y + sliderYOffset, SLIDER_TEXTURE_X + (this.canScroll() ? 0 : SLIDER_TEXTURE_WIDTH), 0, SLIDER_TEXTURE_WIDTH, SLIDER_TEXTURE_HEIGHT);
        this.drawButtons(matrixStack, mouseX, mouseY);
        int recipeAreaStartX = this.guiLeft + RECIPE_AREA_X_OFFSET;
        int recipeAreaStartY = this.guiTop + RECIPE_AREA_Y_OFFSET;
        int lastShownRecipeIndex = this.recipeIndexOffset + resultsMax;
        this.drawRecipeFrames(matrixStack, mouseX, mouseY, recipeAreaStartX, recipeAreaStartY, lastShownRecipeIndex);
        this.drawRecipesItems(recipeAreaStartX, recipeAreaStartY, lastShownRecipeIndex);
    }
    private void drawRecipeFrames(MatrixStack matrixStack, int mouseX, int mouseY, int recipeAreaStartX, int recipeAreaStartY, int lastShownRecipeIndex) {
        for(int i = this.recipeIndexOffset; i < lastShownRecipeIndex && i < this.container.getRecipeListSize(); ++i) {
            int j = i - this.recipeIndexOffset;
            int columnStartX = recipeAreaStartX + j % this.resultsPerLine * RECIPE_TILE_WIDTH;
            int line = j / this.resultsPerLine;
            int lineStartY = recipeAreaStartY + line * RECIPE_TILE_HEIGHT + 2;
            int tileTextureStartY = this.ySize;
            if (i == this.container.getSelectedRecipe()) {
                tileTextureStartY += RECIPE_TILE_HEIGHT;
            } else if (mouseX >= columnStartX && mouseY >= lineStartY && mouseX < columnStartX + RECIPE_TILE_WIDTH && mouseY < lineStartY + RECIPE_TILE_HEIGHT) {
                tileTextureStartY += RECIPE_TILE_HEIGHT * 2;
            }

            this.blit(matrixStack, columnStartX, lineStartY - 1, 0, tileTextureStartY+1, RECIPE_TILE_WIDTH, RECIPE_TILE_HEIGHT);
        }

    }

    private void drawButtons(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.blit(matrixStack, this.guiLeft + BUTTONS_START_X, this.guiTop + BUTTONS_START_Y, BUTTON_TEXTURE_X_OFFSET, BUTTON_TEXTURE_Y_OFFSET + (this.clickedOnAll ? BUTTON_HEIGHT : 0), BUTTON_WIDTH, BUTTON_HEIGHT);
        this.blit(matrixStack, this.guiLeft + BUTTONS_START_X + BUTTON_WIDTH, this.guiTop + BUTTONS_START_Y, BUTTON_TEXTURE_X_OFFSET + BUTTON_WIDTH, BUTTON_TEXTURE_Y_OFFSET + (this.clickedOn64 ? BUTTON_HEIGHT : 0), BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    protected void renderHoveredTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.renderHoveredTooltip(matrixStack, mouseX, mouseY);
        if (this.hasItemsInInputSlot) {
            int i = this.guiLeft + RECIPE_AREA_X_OFFSET;
            int j = this.guiTop + RECIPE_AREA_Y_OFFSET;
            int k = this.recipeIndexOffset + this.resultsMax;
            List<StonecuttingRecipe> list = this.container.getRecipeList();

            for(int l = this.recipeIndexOffset; l < k && l < this.container.getRecipeListSize(); ++l) {
                int i1 = l - this.recipeIndexOffset;
                int j1 = i + i1 % this.resultsPerLine * RECIPE_TILE_WIDTH;
                int k1 = j + i1 / this.resultsPerLine * RECIPE_TILE_HEIGHT + 2;
                if (mouseX >= j1 && mouseX < j1 + RECIPE_TILE_WIDTH && mouseY >= k1 && mouseY < k1 + RECIPE_TILE_HEIGHT) {
                    this.renderTooltip(matrixStack, list.get(l).getRecipeOutput(), mouseX, mouseY);
                }
            }
        }

    }

    private void drawRecipesItems(int left, int top, int recipeIndexOffsetMax) {
        List<StonecuttingRecipe> list = this.container.getRecipeList();

        for(int i = this.recipeIndexOffset; i < recipeIndexOffsetMax && i < this.container.getRecipeListSize(); ++i) {
            int j = i - this.recipeIndexOffset;
            int k = left + j % this.resultsPerLine * RECIPE_TILE_WIDTH;
            int l = j / this.resultsPerLine;
            int i1 = top + l * RECIPE_TILE_HEIGHT + 2;
            this.minecraft.getItemRenderer().renderItemAndEffectIntoGUI(list.get(i).getRecipeOutput(), k, i1);
        }

    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.clickedOnSroll = false;
        this.clickedOnAll = false;
        this.clickedOn64 = false;
        if (this.hasItemsInInputSlot) {
            int i = this.guiLeft + RECIPE_AREA_X_OFFSET;
            int j = this.guiTop + RECIPE_AREA_Y_OFFSET;
            int k = this.recipeIndexOffset + this.resultsMax;

            for(int l = this.recipeIndexOffset; l < k; ++l) {
                int i1 = l - this.recipeIndexOffset;
                double d0 = mouseX - (double)(i + i1 % this.resultsPerLine * RECIPE_TILE_WIDTH);
                double d1 = mouseY - (double)(j + i1 / this.resultsPerLine * RECIPE_TILE_HEIGHT);
                if (d0 >= 0.0D && d1 >= 0.0D && d0 < 16.0D && d1 < 18.0D && this.container.selectRecipe(this.minecraft.player, l)) {
                    Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.playerController.sendEnchantPacket((this.container).windowId, l);
                    return true;
                }
            }

            i = this.guiLeft + SLIDER_X;
            j = this.guiTop + SLIDER_Y;
            if (mouseX >= (double)i && mouseX < (double)(i + SLIDER_TEXTURE_WIDTH) && mouseY >= (double)j && mouseY < (double)(j + 54)) {
                this.clickedOnSroll = true;
            }

            i = this.guiLeft + BUTTONS_START_X;
            j = this.guiTop + BUTTONS_START_Y;
            if (mouseX >= (double)i && mouseX < (double)(i + BUTTON_WIDTH) && mouseY >= (double)j && mouseY < (double)(j + BUTTON_HEIGHT)) {
                this.clickedOnAll = true;
                this.container.craftAll(this.minecraft.player);
            }

            i = this.guiLeft + BUTTONS_START_X + BUTTON_WIDTH;
            j = this.guiTop + BUTTONS_START_Y;
            if (mouseX >= (double)i && mouseX < (double)(i + BUTTON_WIDTH) && mouseY >= (double)j && mouseY < (double)(j + BUTTON_HEIGHT)) {
                this.clickedOn64 = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.clickedOnSroll && this.canScroll()) {
            int i = this.guiTop + SLIDER_Y;
            int j = i + 54;
            this.sliderProgress = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.sliderProgress = MathHelper.clamp(this.sliderProgress, 0.0F, 1.0F);
            this.recipeIndexOffset = (int)((double)(this.sliderProgress * (float)this.getHiddenRows()) + 0.5D) * this.resultsPerLine;
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.canScroll()) {
            int i = this.getHiddenRows();
            this.sliderProgress = (float)((double)this.sliderProgress - delta / (double)i);
            this.sliderProgress = MathHelper.clamp(this.sliderProgress, 0.0F, 1.0F);
            this.recipeIndexOffset = (int)((double)(this.sliderProgress * (float)i) + 0.5D) * this.resultsPerLine;
        }

        return true;
    }

    private boolean canScroll() {
        return this.hasItemsInInputSlot && this.container.getRecipeListSize() > this.resultsMax;
    }

    protected int getHiddenRows() {
        return (this.container.getRecipeListSize() + this.resultsPerLine - 1) / this.resultsPerLine - 3;
    }

    /**
     * Called every time this screen's container is changed (is marked as dirty).
     */
    private void onInventoryUpdate() {
        this.hasItemsInInputSlot = this.container.hasItemsinInputSlot();
        if (!this.hasItemsInInputSlot) {
            this.sliderProgress = 0.0F;
            this.recipeIndexOffset = 0;
        }

    }
}
