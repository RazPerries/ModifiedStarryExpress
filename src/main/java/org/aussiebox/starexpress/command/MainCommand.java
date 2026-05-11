package org.aussiebox.starexpress.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.aussiebox.starexpress.cca.SilenceComponent;

public class MainCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("starexpress")
                        .then(Commands.literal("silenceme")
                                .requires(ctx -> ctx.hasPermission(2))
                                .executes(MainCommand::silenceMe)
                        )
        );
    }

    private static int silenceMe(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();

        SilenceComponent.KEY.get(player).setSilenced(true);
        SilenceComponent.KEY.get(player).setSilencer(player.getUUID());
        SilenceComponent.KEY.get(player).setTearChecks(0);
        SilenceComponent.KEY.get(player).sync();

        return 1;
    }
}
