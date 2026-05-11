package org.aussiebox.starexpress.client.mixin.muzzler;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.aussiebox.starexpress.client.StarryExpressClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public class SilencedHudMixin {

    @Inject(
            method = "renderHud",
            at = @At("TAIL")
    )
    private static void silencedTip(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (StarryExpressClient.target == null) return;

        SilenceComponent victimSilence = SilenceComponent.KEY.get(StarryExpressClient.target);
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        if (!victimSilence.isSilenced()) return;
        if (gameWorldComponent.isInnocent(player) && !player.isSpectator() && !player.isCreative()) return;

        renderSilencedTip(renderer, context);
    }

    @Unique
    private static void renderSilencedTip(Font renderer, GuiGraphics context) {
        Component text = Component.translatable("tip.starexpress.muzzler.silenced");

        context.pose().pushPose();
        context.pose().translate(context.guiWidth() / 2.0F, context.guiHeight() / 2.0f - 37.5F, 0.0F);
        context.pose().scale(0.6F, 0.6F, 1.0F);

        context.drawString(
                renderer,
                text,
                -renderer.width(text) / 2,
                32,
                StarryExpressRoles.MUZZLER.color()
        );

        context.pose().popPose();
    }

}
