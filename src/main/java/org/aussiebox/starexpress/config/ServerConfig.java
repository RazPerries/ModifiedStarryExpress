package org.aussiebox.starexpress.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "starexpress-server", wrapperName = "StarryExpressServerConfig")
public class ServerConfig {

    @SectionHeader("role_config")

    @Comment("Config options related to the Starstruck role.")
    @Nest public StarstruckConfig starstruckConfig = new StarstruckConfig();
    @Comment("Config options related to the Muzzler role.")
    @Nest public MuzzlerConfig muzzlerConfig = new MuzzlerConfig();
    @Comment("Config options related to the Allergic modifier.")
    @Nest public AllergicConfig allergicConfig = new AllergicConfig();

    public static class StarstruckConfig {

        @Comment("When enabled, completing a task as the Starstruck will reduce its ability cooldown.")
        public boolean taskReducesCooldown = true;

        @Comment("The number of seconds to reduce the Starstruck's ability cooldown by upon task completion.")
        @RangeConstraint(min = 1, max = 600)
        public int taskCooldownReduction = 5;

        @Comment("The Starstruck's ability cooldown, in seconds.")
        @RangeConstraint(min = 1, max = 600)
        public int abilityCooldown = 90;

        @Comment("The Starstruck's ability duration, in seconds.")
        @RangeConstraint(min = 1, max = 600)
        public int abilityDuration = 15;

        @Comment("When enabled, the Starstruck's movement speed will be changed while their ability is active.")
        public boolean abilityAffectsMovementSpeed = true;

        @Comment("The Starstruck's movement speed while walking with their ability active. (Default: 0.12F, WATHE Default: 0.07F)")
        @RangeConstraint(min = 0.07F, max = 10.0F)
        public float abilityWalkSpeed = 0.12F;

        @Comment("The Starstruck's movement speed while sprinting with their ability active. (Default: 0.15F, WATHE Default: 0.1F)")
        @RangeConstraint(min = 0.1F, max = 10.0F)
        public float abilitySprintSpeed = 0.15F;

    }
    public static class AllergicConfig {

        @Comment("The chance of Allergic players receiving no effects upon their allergy triggering. Set to 0 to disable.")
        public int nothingChance = 3;

        @Comment("The chance of Allergic players receiving instinct upon their allergy triggering. Set to 0 to disable.")
        public int instinctChance = 1;

        @Comment("The chance of Allergic players receiving armor upon their allergy triggering. Set to 0 to disable.")
        public int armorChance = 1;

        @Comment("The chance of Allergic players being poisoned upon their allergy triggering. Set to 0 to disable.")
        public int poisonChance = 1;

        @Comment("The duration, in seconds, of the Allergic's instinct effect.")
        public int instinctDuration = 3;

    }
    public static class MuzzlerConfig {

        @Comment("The cooldown, in seconds, of the Tape item when the Muzzler uses it on a player.")
        @RangeConstraint(min = 0, max = 600)
        public int tapeCooldown = 20;

        @Comment("The time, in seconds, it takes for a silenced player to suffocate when outside. Set to 0 to disable.")
        @RangeConstraint(min = 0, max = 600)
        public int suffocationTime = 60;

        @Comment("The number of times players need to tear the tape off of a silenced player to unsilence them. Set to 0 to disable this feature.")
        @RangeConstraint(min = 0, max = 1337)
        public int tapeTearCheckCount = 4;

        @Comment("The amount of mood, on a scale of 0.0 to 1.0, that is taken from a player when another player attempts to tear their silencing tape off.")
        @RangeConstraint(min = 0.0F, max = 1.0F)
        public float tapeTearMoodChange = 0.05F;

        @Comment("When enabled, completing a tape check on a player with 0.0 mood remaining will kill the checked player.")
        public boolean killIfCheckedAtZero = true;

        @Comment("The delay, in seconds, of the \"This player is Silenced\" tip showing up. Set to 0 to disable.")
        @RangeConstraint(min = 0, max = 600)
        public int displaySilencedTipDelay = 120;

    }
}