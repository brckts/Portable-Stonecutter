package xyz.brckts.portablestonecutter.util.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHelper {
    // TODO: Partial item rendering
    // TODO: Overlay with EPSC on non-normal mode

    public static void renderGhostItem(PoseStack ms, Minecraft mc, ItemStack is, int x, int y) {
        ItemRenderer itemrenderer = mc.getItemRenderer();
        itemrenderer.renderAndDecorateFakeItem(is, x, y);
        RenderSystem.depthFunc(516);
        GuiComponent.fill(ms, x, y, x + 16, y + 16, 822083583);
        RenderSystem.depthFunc(515);
    }
}
