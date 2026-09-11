package org.aussiebox.starexpress.item.custom;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.jetbrains.annotations.NotNull;

public class TapeItem extends Item {
    public TapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getDefaultMaxStackSize() {
        return 1;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack, @NotNull Player player, @NotNull LivingEntity livingEntity, @NotNull InteractionHand interactionHand) {
        super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);

        if (!(livingEntity instanceof Player victim)) return InteractionResult.FAIL;

        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) return InteractionResult.FAIL;

        if (player.getCooldowns().isOnCooldown(itemStack.getItem())) return InteractionResult.FAIL;

        SilenceComponent victimSilence = SilenceComponent.KEY.get(victim);

        if (victimSilence.isSilenced()) return InteractionResult.FAIL;

        if (!player.isCreative()) {
            player.getInventory().removeItem(itemStack);
            player.getCooldowns().addCooldown(itemStack.getItem(), StarryExpress.CONFIG.muzzlerConfig.tapeCooldown() * 20);
        }

        victimSilence.setSilenced(true);
        victimSilence.setSilencer(player.getUUID());
        victimSilence.setTearChecks(0);
        victimSilence.sync();

        return InteractionResult.PASS;
    }
}
