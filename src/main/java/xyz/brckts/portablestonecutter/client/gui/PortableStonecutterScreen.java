package xyz.brckts.portablestonecutter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;
import xyz.brckts.portablestonecutter.network.MessageButtonPressed;
import xyz.brckts.portablestonecutter.network.MessageLockRecipe;
import xyz.brckts.portablestonecutter.network.MessageSelectRecipe;
import xyz.brckts.portablestonecutter.network.NetworkHandler;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PortableStonecutterScreen extends ContainerScreen<PortableStonecutterContainer> {

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

    public PortableStonecutterScreen(PortableStonecutterContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        screenContainer.setInventoryUpdateListener(this::onInventoryUpdate);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 175;
        this.imageHeight = 165;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        super.renderLabels(matrixStack, x, y);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(matrixStack);
        this.minecraft.getTextureManager().bind(BACKGROUND_TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int sliderYOffset = (int)((float)SLIDER_MAX_Y_OFFSET * this.sliderProgress);
        this.blit(matrixStack, this.leftPos + SLIDER_X, this.topPos + SLIDER_Y + sliderYOffset, SLIDER_TEXTURE_X + (this.canScroll() ? 0 : SLIDER_TEXTURE_WIDTH), 0, SLIDER_TEXTURE_WIDTH, SLIDER_TEXTURE_HEIGHT);
        this.drawButtons(matrixStack, mouseX, mouseY);
        int recipeAreaStartX = this.leftPos + RECIPE_AREA_X_OFFSET;
        int recipeAreaStartY = this.topPos + RECIPE_AREA_Y_OFFSET;
        int lastShownRecipeIndex = this.recipeIndexOffset + RESULTS_MAX;
        this.drawRecipeFrames(matrixStack, mouseX, mouseY, recipeAreaStartX, recipeAreaStartY, lastShownRecipeIndex);
        this.drawRecipesItems(recipeAreaStartX, recipeAreaStartY, lastShownRecipeIndex);
        if (this.menu.isRecipeLocked()) this.drawLockedItem();
    }
    private void drawRecipeFrames(MatrixStack matrixStack, int mouseX, int mouseY, int recipeAreaStartX, int recipeAreaStartY, int lastShownRecipeIndex) {
        for(int i = this.recipeIndexOffset; i < lastShownRecipeIndex && i < this.menu.getRecipeListSize(); ++i) {
            int j = i - this.recipeIndexOffset;
            int columnStartX = recipeAreaStartX + j % RESULTS_PER_LINE * RECIPE_TILE_WIDTH;
            int line = j / RESULTS_PER_LINE;
            int lineStartY = recipeAreaStartY + line * RECIPE_TILE_HEIGHT + 2;
            int tileTextureStartY = this.imageHeight;
            if (i == this.menu.getSelectedRecipe()) {
                tileTextureStartY += RECIPE_TILE_HEIGHT;
            } else if (mouseX >= columnStartX && mouseY >= lineStartY && mouseX < columnStartX + RECIPE_TILE_WIDTH && mouseY < lineStartY + RECIPE_TILE_HEIGHT) {
                tileTextureStartY += RECIPE_TILE_HEIGHT * 2;
            }

            this.blit(matrixStack, columnStartX, lineStartY - 1, 0, tileTextureStartY+1, RECIPE_TILE_WIDTH, RECIPE_TILE_HEIGHT);
        }
    }

    private void drawButtons(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.blit(matrixStack, this.leftPos + BUTTONS_START_X, this.topPos + BUTTONS_START_Y, BUTTON_TEXTURE_X_OFFSET, BUTTON_TEXTURE_Y_OFFSET + (this.clickedOnAll ? BUTTON_HEIGHT : 0), BUTTON_WIDTH, BUTTON_HEIGHT);
        this.blit(matrixStack, this.leftPos + BUTTONS_START_X + BUTTON_WIDTH, this.topPos + BUTTONS_START_Y, BUTTON_TEXTURE_X_OFFSET + BUTTON_WIDTH, BUTTON_TEXTURE_Y_OFFSET + (this.clickedOn64 ? BUTTON_HEIGHT : 0), BUTTON_WIDTH, BUTTON_HEIGHT);
        if (this.menu.isLockable() || this.menu.isRecipeLocked()) {
            this.blit(matrixStack, this.leftPos + LOCK_BUTTON_X, this.topPos + LOCK_BUTTON_Y, BUTTON_TEXTURE_X_OFFSET, BUTTON_TEXTURE_Y_OFFSET + BUTTON_HEIGHT * 2 + (this.menu.isRecipeLocked() ? LOCK_BUTTON_HEIGHT : 0), LOCK_BUTTON_WIDTH, LOCK_BUTTON_HEIGHT);
        }
    }

    protected void renderHoveredTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.renderTooltip(matrixStack, mouseX, mouseY);
        if (this.hasItemsInInputSlot) {
            int i = this.leftPos + RECIPE_AREA_X_OFFSET;
            int j = this.topPos + RECIPE_AREA_Y_OFFSET;
            int k = this.recipeIndexOffset + RESULTS_MAX;
            List<StonecuttingRecipe> list = this.menu.getRecipeList();

            for(int l = this.recipeIndexOffset; l < k && l < this.menu.getRecipeListSize(); ++l) {
                int i1 = l - this.recipeIndexOffset;
                int j1 = i + i1 % RESULTS_PER_LINE * RECIPE_TILE_WIDTH;
                int k1 = j + i1 / RESULTS_PER_LINE * RECIPE_TILE_HEIGHT + 2;
                if (mouseX >= j1 && mouseX < j1 + RECIPE_TILE_WIDTH && mouseY >= k1 && mouseY < k1 + RECIPE_TILE_HEIGHT) {
                    this.renderTooltip(matrixStack, list.get(l).getResultItem(), mouseX, mouseY);
                }
            }
        }
    }

    private void drawRecipesItems(int left, int top, int recipeIndexOffsetMax) {
        List<StonecuttingRecipe> list = this.menu.getRecipeList();

        for(int i = this.recipeIndexOffset; i < recipeIndexOffsetMax && i < this.menu.getRecipeListSize(); ++i) {
            int j = i - this.recipeIndexOffset;
            int k = left + j % RESULTS_PER_LINE * RECIPE_TILE_WIDTH;
            int l = j / RESULTS_PER_LINE;
            int i1 = top + l * RECIPE_TILE_HEIGHT + 2;
            this.minecraft.getItemRenderer().renderAndDecorateItem(list.get(i).getResultItem(), k, i1);
        }
    }

    //TODO: Render transparent
    private void drawLockedItem() {
        if (this.menu.getLockedRecipe() != null && this.menu.getLockedInput() != null) {
            this.minecraft.getItemRenderer().renderGuiItem(new ItemStack(this.menu.getLockedInput()), this.leftPos + INPUT_X, this.topPos + INPUT_Y);
            this.minecraft.getItemRenderer().renderGuiItem(this.menu.getLockedRecipe().getResultItem(), this.leftPos + OUTPUT_X, this.topPos + OUTPUT_Y);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.clickedOnScroll = false;
        this.clickedOnAll = false;
        this.clickedOn64 = false;
        if (this.hasItemsInInputSlot) {
            int i = this.leftPos + RECIPE_AREA_X_OFFSET;
            int j = this.topPos + RECIPE_AREA_Y_OFFSET;
            int k = this.recipeIndexOffset + RESULTS_MAX;

            for (int l = this.recipeIndexOffset; l < k; ++l) {
                int i1 = l - this.recipeIndexOffset;
                double d0 = mouseX - (double) (i + i1 % RESULTS_PER_LINE * RECIPE_TILE_WIDTH);
                double d1 = mouseY - (double) (j + i1 / RESULTS_PER_LINE * RECIPE_TILE_HEIGHT);
                if (d0 >= 0.0D && d1 >= 0.0D && d0 < 16.0D && d1 < 18.0D && this.menu.selectRecipe(l)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    NetworkHandler.channel.sendToServer(new MessageSelectRecipe(l));
                    return true;
                }
            }

            i = this.leftPos + SLIDER_X;
            j = this.topPos + SLIDER_Y;
            if (mouseX >= (double) i && mouseX < (double) (i + SLIDER_TEXTURE_WIDTH) && mouseY >= (double) j && mouseY < (double) (j + 54)) {
                this.clickedOnScroll = true;
            }

            i = this.leftPos + BUTTONS_START_X;
            j = this.topPos + BUTTONS_START_Y;
            if (mouseX >= (double) i && mouseX < (double) (i + BUTTON_WIDTH) && mouseY >= (double) j && mouseY < (double) (j + BUTTON_HEIGHT)) {
                this.clickedOnAll = true;
                NetworkHandler.channel.sendToServer(new MessageButtonPressed(MessageButtonPressed.CRAFT_ALL_BUTTON));
            }

            i = this.leftPos + BUTTONS_START_X + BUTTON_WIDTH;
            j = this.topPos + BUTTONS_START_Y;
            if (mouseX >= (double) i && mouseX < (double) (i + BUTTON_WIDTH) && mouseY >= (double) j && mouseY < (double) (j + BUTTON_HEIGHT)) {
                this.clickedOn64 = true;
                NetworkHandler.channel.sendToServer(new MessageButtonPressed(MessageButtonPressed.CRAFT_64_BUTTON));
            }
        }

        int i = this.leftPos + LOCK_BUTTON_X;
        int j = this.topPos + LOCK_BUTTON_Y;
        if (mouseX >= (double)i && mouseX < (double)(i + LOCK_BUTTON_WIDTH) && mouseY >= (double)j && mouseY < (double)(j + LOCK_BUTTON_HEIGHT)) {
            if (this.menu.isRecipeLocked()) {
                this.menu.setRecipeLocked(false);
                NetworkHandler.channel.sendToServer(new MessageLockRecipe(this.menu.getSelectedRecipe(), false));
            } else if (this.menu.getSelectedRecipe() != -1) {
                this.menu.setRecipeLocked(true);
                NetworkHandler.channel.sendToServer(new MessageLockRecipe(this.menu.getSelectedRecipe(), true));
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.clickedOnScroll && this.canScroll()) {
            int i = this.topPos + SLIDER_Y;
            int j = i + 54;
            this.sliderProgress = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.sliderProgress = MathHelper.clamp(this.sliderProgress, 0.0F, 1.0F);
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
            this.sliderProgress = MathHelper.clamp(this.sliderProgress, 0.0F, 1.0F);
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
        return this.hasItemsInInputSlot && this.menu.getRecipeListSize() > RESULTS_MAX;
    }

    protected int getHiddenRows() {
        return (this.menu.getRecipeListSize() + RESULTS_PER_LINE - 1) / RESULTS_PER_LINE - LINES_SHOWN;
    }

    /**
     * Called every time this screen's container is changed (is marked as dirty).
     */
    private void onInventoryUpdate() {
        this.hasItemsInInputSlot = this.menu.hasItemsInInputSlot();
        if (!this.hasItemsInInputSlot) {
            this.sliderProgress = 0.0F;
            this.recipeIndexOffset = 0;
        }
    }
}
