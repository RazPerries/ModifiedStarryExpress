package org.aussiebox.starexpress.mixin.starstruck;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerStaminaComponent;
import net.minecraft.world.entity.player.Player;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerStaminaComponent.class)
public class StarstruckInfiniteStamina {

    @Shadow @Final @NotNull private Player player;

    @ModifyReturnValue(method = "getStaminaDrain", at = @At("RETURN"))
    private float starstruckInfiniteStamina(float original) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(this.player.level());
        if (gameWorld.isRole(this.player, StarryExpressRoles.STARSTRUCK)) {
            StarstruckComponent starstruckComponent = StarstruckComponent.KEY.get(this.player);
            if (starstruckComponent.ticks > 0) {
                return 0f;
            }
        }
        return original;
    }
}