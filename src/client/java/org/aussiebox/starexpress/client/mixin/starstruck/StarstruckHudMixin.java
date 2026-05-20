package org.aussiebox.starexpress.client.mixin.starstruck;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.AbilityComponent;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.aussiebox.starexpress.client.StarryExpressClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class StarstruckHudMixin {
    @Shadow public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void starstruckHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) return;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(Minecraft.getInstance().player.level());
        AbilityComponent abilityComponent = AbilityComponent.KEY.get(Minecraft.getInstance().player);
        StarstruckComponent starstruckComponent = StarstruckComponent.KEY.get(Minecraft.getInstance().player);
        if (gameWorldComponent.isRole(Minecraft.getInstance().player, StarryExpressRoles.STARSTRUCK)) {
            int drawY = context.guiHeight();

            Component line = Component.translatable("tip.starexpress.starstruck", StarryExpressClient.abilityBind.getTranslatedKeyMessage(), starstruckComponent.abilityCost);

            if (abilityComponent.cooldown > 0) {
                line = Component.translatable("tip.starexpress.cooldown", abilityComponent.cooldown/20);
            }

            if (starstruckComponent.ticks > 0) {
                line = Component.translatable("tip.starexpress.active_timer", starstruckComponent.ticks/20);
            }

            drawY -= getFont().wordWrapHeight(line, 999999);
            context.drawString(getFont(), line, context.guiWidth() - getFont().width(line), drawY, StarryExpressRoles.STARSTRUCK.color());
        }
    }
}