package org.aussiebox.starexpress.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.client.gui.screen.GuidebookScreen;
import org.jetbrains.annotations.NotNull;

public class GuidebookButtonWidget extends Button {
    public GuidebookButtonWidget(int x, int y) {
        super(x, y, 16, 16, Component.empty(), button -> Minecraft.getInstance().setScreen(new GuidebookScreen()), DEFAULT_NARRATION);
    }

    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        context.blitSprite(StarryExpress.id("gui/guidebook_slot"), this.getX() - 7, this.getY() - 7, 30, 30);
        context.renderItem(Items.KNOWLEDGE_BOOK.getDefaultInstance(), this.getX(), this.getY());
        if (this.isHovered()) {
            this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
            context.renderTooltip(Minecraft.getInstance().font, Component.translatable("guidebook.tooltip.open"), Minecraft.getInstance().font.width(Component.translatable("guidebook.tooltip.open")) / 2 - 10, this.getY() + 16);
        }
    }

    private void drawShopSlotHighlight(GuiGraphics context, int x, int y, int z) {
        int color = 0x495CFA86;
        context.fillGradient(RenderType.guiOverlay(), x, y, x + 16, y + 16, color, color, z);
    }
}