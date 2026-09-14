package org.aussiebox.starexpress;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.aussiebox.starexpress.block.ModBlocks;
import org.aussiebox.starexpress.block.entity.ModBlockEntities;
import org.aussiebox.starexpress.cca.AbilityComponent;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.aussiebox.starexpress.cca.StarstruckComponent;
import org.aussiebox.starexpress.config.StarryExpressServerConfig;
import org.aussiebox.starexpress.item.StarryExpressItems;
import org.aussiebox.starexpress.packet.AbilityC2SPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarryExpress implements ModInitializer {

    public static String MOD_ID = "starexpress";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final StarryExpressServerConfig CONFIG = StarryExpressServerConfig.createAndLoad();

    @Override
    public void onInitialize() {
        ModSounds.init();
        ModBlockEntities.init();
        ModBlocks.init();
        StarryExpressItems.init();

        StarryExpressCommands.init();
        StarryExpressRoles.init();
        StarryExpressModifiers.init();

        PayloadTypeRegistry.playC2S().register(AbilityC2SPacket.TYPE, AbilityC2SPacket.CODEC);

        registerPackets();
        registerEvents();
    }

    public void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(AbilityC2SPacket.TYPE, (payload, context) -> {
            AbilityComponent abilityComponent = AbilityComponent.KEY.get(context.player());
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(context.player().level());

            if (!GameFunctions.isPlayerAliveAndSurvival(context.player())) return;
            if (gameWorldComponent.isRole(context.player(), StarryExpressRoles.STARSTRUCK) && abilityComponent.cooldown <= 0) {
                PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());
                StarstruckComponent starstruckComponent = StarstruckComponent.KEY.get(context.player());
                if (playerShopComponent.balance >= starstruckComponent.abilityCost) {
                    abilityComponent.setCooldown(CONFIG.starstruckConfig.abilityCooldown() * 20);
                    StarstruckComponent.KEY.get(context.player()).setTicks(CONFIG.starstruckConfig.abilityDuration() * 20);
                    playerShopComponent.setBalance(playerShopComponent.balance - starstruckComponent.abilityCost);
                } else {
                    context.player().displayClientMessage(Component.translatable("tip.starexpress.cannot_afford", starstruckComponent.abilityCost).withColor(StarryExpressRoles.STARSTRUCK.color()), true);
                }
            }
        });
    }

    public void registerEvents() {
        ModdedRoleAssigned.EVENT.register((player,role)->{
            if (role.equals(StarryExpressRoles.MUZZLER)) {
                player.addItem(StarryExpressItems.TAPE.getDefaultInstance());
            }
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {

            if (!(entity instanceof Player victim)) return InteractionResult.PASS;
            if (CONFIG.muzzlerConfig.tapeTearCheckCount() == 0) return InteractionResult.PASS;

            if (player.getMainHandItem().isEmpty() && !player.isSpectator() && player.isShiftKeyDown()) {
                SilenceComponent victimSilence = SilenceComponent.KEY.get(victim);
                if (!victimSilence.isSilenced()) return InteractionResult.PASS;
                if (SilenceComponent.KEY.get(player).isSilenced()) return InteractionResult.PASS;

                victimSilence.setTearChecks(victimSilence.getTearChecks() + 1);

                //Optimized? No. I'll fix this later ig
                if (victimSilence.getTearChecks() >= 4) {
                    victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.ITEM_TAPE_APPLY, SoundSource.PLAYERS, 1.0F, 2.0F);
                }

                if (victimSilence.getTearChecks() >= CONFIG.muzzlerConfig.tapeTearCheckCount()) victimSilence.setSilenced(false);

                victimSilence.sync();

                // Hurt mood of remover
                PlayerMoodComponent playerMood = PlayerMoodComponent.KEY.get(player);
                playerMood.setMood(playerMood.getMood() - CONFIG.muzzlerConfig.tapeTearMoodChange());
                playerMood.sync();

                // Hurt mood of taped
                PlayerMoodComponent victimMood = PlayerMoodComponent.KEY.get(victim);
                victimMood.setMood(victimMood.getMood() - CONFIG.muzzlerConfig.tapeTearMoodChange());
                victimMood.sync();

                if (victimMood.getMood() <= 0.0F) {
                    GameFunctions.killPlayer(victim, true, victim.level().getPlayerByUUID(victimSilence.getSilencer()), StarryExpressConstants.SILENCED_TAPE_REMOVED_DEATH_REASON);
                }

                if (victimSilence.getTearChecks() >= 4) {
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
        });

    }

    public static ResourceLocation id(String key) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, key);
    }

}
