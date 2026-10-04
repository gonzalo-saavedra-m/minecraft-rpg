package soulsstats.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.SoulsStats;
import soulsstats.display.Text;
import soulsstats.stat.Stat;

/** `/soulsstats raise <stat> [amount]`: el jugador reparte sus puntos libres en una stat (1 si no dice cuántos). */
final class RaiseCommand {
	static final SubCommand COMMAND = new SubCommand("raise", "spends your free points on a stat (1 if you don't say how many)", RaiseCommand::node);

	private static LiteralArgumentBuilder<CommandSourceStack> node(LiteralArgumentBuilder<CommandSourceStack> literal) {
		// ponytail: dos mods con stats del mismo path chocan en el comando; usar el id completo si pasa.
		for (Stat stat : Stat.REGISTRY) {
			literal.then(Commands.literal(stat.id().getPath()).executes(command -> raise(command, stat, 1))
					.then(Commands.argument("amount", IntegerArgumentType.integer(1))
							.executes(command -> raise(command, stat, IntegerArgumentType.getInteger(command, "amount")))));
		}
		return literal;
	}

	private static int raise(CommandContext<CommandSourceStack> command, Stat stat, int amount) throws CommandSyntaxException {
		ServerPlayer player = command.getSource().getPlayerOrException();
		if (!SoulsStats.raise(player, stat, amount)) {
			command.getSource().sendFailure(Text.tr("not_enough_points", "You have %s free points", SoulsStats.progress(player).freePoints()));
			return 0;
		}
		return amount;
	}
}
