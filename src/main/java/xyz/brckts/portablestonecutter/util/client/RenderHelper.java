package xyz.brckts.portablestonecutter.util.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHelper {
    // TODO: Partial item rendering
    // TODO: Overlay with EPSC on non-normal mode

    public static void renderGhostItem(MatrixStack ms, Minecraft mc, ItemStack is, int x, int y) {
        ItemRenderer itemrenderer = mc.getItemRenderer();
        itemrenderer.renderAndDecorateFakeItem(is, x, y);
        RenderSystem.depthFunc(516);
        AbstractGui.fill(ms, x, y, x + 16, y + 16, 822083583);
        RenderSystem.depthFunc(515);
    }
}
