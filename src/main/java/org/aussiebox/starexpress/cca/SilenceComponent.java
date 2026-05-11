package org.aussiebox.starexpress.cca;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.aussiebox.starexpress.StarryExpress;
import org.aussiebox.starexpress.StarryExpressConstants;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Objects;
import java.util.UUID;

public class SilenceComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<SilenceComponent> KEY = ComponentRegistry.getOrCreate(StarryExpress.id("silence"), SilenceComponent.class);

    private final Player player;
    public float drainStart = 20;
    public float silencedDrain = 0.0f;
    public int silencedInterval = 20;
    public int silencedKillTimer = 180;

    @Setter
    @Getter
    private boolean silenced;

    @Setter
    @Getter
    private UUID silencer;

    private int outsideTicks;

    @Setter
    @Getter
    private int tearChecks;

    @Getter
    private int silencedTicks;

    public SilenceComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        if (silenced) silencedTicks++;
        else silencedTicks = 0;

        if (Wathe.isSkyVisibleAdjacent(player) && silenced) outsideTicks++;
        else outsideTicks = 0;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());

        if (!Noellesroles.KILLER_SIDED_NEUTRALS.contains(gameWorldComponent.getRole(player)) && !gameWorldComponent.canUseKillerFeatures(player)) {
            if (StarryExpress.CONFIG.muzzlerConfig.suffocationTime() > 0) {
                if (outsideTicks >= StarryExpress.CONFIG.muzzlerConfig.suffocationTime() * 20)
                    GameFunctions.killPlayer(player, true, player.level().getPlayerByUUID(silencer), StarryExpressConstants.SILENCED_OUTSIDE_DEATH_REASON);
            }

            PlayerMoodComponent silencedMood = PlayerMoodComponent.KEY.get(player);

            // Start draining player mood
            if (silencedTicks >= drainStart * 20 && !silencedMood.tasks.isEmpty() && silencedDrain != 0.0f) {
                silencedMood.setMood(silencedMood.getMood() - (silencedDrain / 20));
                silencedMood.sync();
            }

            // If these initial interval passes, then increase mood drain by a little bit
            if (silencedTicks == silencedInterval * 20) {
                this.silencedDrain += 0.005f;
                this.silencedInterval += 20;
            }

            // If silenced player is 60 seconds away from dying, warn them
            if (silencedTicks == (silencedKillTimer - 60) * 20) {
                this.player.displayClientMessage(Component.literal("You feel light headed. You will collapse soon without help.").withStyle(ChatFormatting.BLUE), true);
            }

            // Kill silenced player after this time passes still silenced
            if (this.silencedTicks == silencedKillTimer * 20) {
                silencedMood.setMood(silencedMood.getMood() - 1f);
                silencedMood.sync();
            }

            // Kill Player if they reach 0 mood
            if (silencedMood.getMood() <= 0.0F && silenced) {
                GameFunctions.killPlayer(player, true, player.level().getPlayerByUUID(silencer), StarryExpressConstants.SILENCED_OUTSIDE_DEATH_REASON);
            }

            this.sync();
        }
    }

    public void reset() {
        this.silenced = false;
        this.silencer = null;
        this.outsideTicks = 0;
        this.tearChecks = 0;
        this.silencedTicks = 0;
        this.silencedDrain = 0.0f;
        this.silencedInterval = 20;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }


    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        this.silenced = tag.contains("silenced") && tag.getBoolean("silenced");
        this.silencer = tag.contains("silencer") ? tag.getUUID("silencer") : null;
        this.outsideTicks = tag.contains("outside_ticks") ? tag.getInt("outside_ticks") : 0;
        this.tearChecks = tag.contains("tear_checks") ? tag.getInt("tear_checks") : 0;
        this.silencedTicks = tag.contains("silenced_ticks") ? tag.getInt("silenced_ticks") : 0;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        tag.putBoolean("silenced", this.silenced);
        tag.putUUID("silencer", Objects.requireNonNullElseGet(this.silencer, () -> UUID.fromString("e1e89fbb-3beb-492a-b1be-46a4ce19c9d1")));
        tag.putInt("outside_ticks", this.outsideTicks);
        tag.putInt("tear_checks", this.tearChecks);
        tag.putInt("silenced_ticks", this.silencedTicks);
    }

}
