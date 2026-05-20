package org.aussiebox.starexpress.client.mixin.starstruck;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.gui.StoreRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StoreRenderer.class)
public abstract class StarstruckStoreRenderMixin {

    @Shadow
    public static StoreRenderer.MoneyNumberRenderer view;

    @Shadow public static float offsetDelta;

    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void renderCoinsForCustomRoles(Font renderer, LocalPlayer player, GuiGraphics context, float delta, CallbackInfo ci) {
        if ((GameWorldComponent.KEY.get(player.level())).isRole(player.getUUID(), StarryExpressRoles.STARSTRUCK)) {
            int balance = (PlayerShopComponent.KEY.get(player)).balance;
            if (view.getTarget() != (float)balance) {
                offsetDelta = (float)balance > view.getTarget() ? 0.6F : -0.6F;
                view.setTarget((float)balance);
            }

            float r = offsetDelta > 0.0F ? 1.0F - offsetDelta : 1.0F;
            float g = offsetDelta < 0.0F ? 1.0F + offsetDelta : 1.0F;
            float b = 1.0F - Math.abs(offsetDelta);
            int colour = Mth.color(r, g, b) | -16777216;
            context.pose().pushPose();
            context.pose().translate((float)(context.guiWidth() - 12), 6.0F, 0.0F);
            view.render(renderer, context, 0, 0, colour, delta);
            context.pose().popPose();
            offsetDelta = Mth.lerp(delta / 16.0F, offsetDelta, 0.0F);
        }
    }
}
