package xyz.brckts.portablestonecutter.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHelper {
    // TODO: Partial item rendering
    // TODO: Overlay with EPSC on non-normal mode

    public static void renderGhostItem(GuiGraphics guiGraphics, Minecraft mc, ItemStack is, int x, int y) {
        guiGraphics.renderItem(is, x, y);
        int color = 0x308b8b8b;
        guiGraphics.fillGradient(RenderType.guiOverlay(), x, y, x + 16, y + 16, color, color, 0);
    }
}
