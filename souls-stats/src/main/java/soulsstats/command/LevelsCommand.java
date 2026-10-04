package soulsstats.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.SoulsStats;
import soulsstats.display.Text;

/** `/soulsstats levels <players> <amount>` (admins): sube niveles, o los baja con negativo sin bajar de 1. */
final class LevelsCommand {
	static final SubCommand COMMAND = new SubCommand("levels", "raises levels, or lowers them with a negative number (admins)", LevelsCommand::node);

	private static LiteralArgumentBuilder<CommandSourceStack> node(LiteralArgumentBuilder<CommandSourceStack> literal) {
		return literal.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(command -> {
							int amount = IntegerArgumentType.getInteger(command, "amount");
							Collection<ServerPlayer> players = EntityArgument.getPlayers(command, "players");
							players.forEach(player -> SoulsStats.giveLevels(player, amount));
							command.getSource().sendSuccess(() -> Text.tr("levels_given", "%s levels to %s player(s)", amount, players.size()).withStyle(ChatFormatting.GRAY), true);
							return players.size();
						})));
	}
}
