package soulsstats.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.function.ObjIntConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.SoulsStats;
import soulsstats.display.Text;
import soulsstats.stat.Stat;

/**
 * `/soulsstats points <players> <amount> [stat]` (admins): sin stat da puntos libres que no vienen del nivel; con
 * stat, puntos fijos en ella que el respec no devuelve (clases base). Negativo los quita.
 */
final class PointsCommand {
	static final SubCommand COMMAND = new SubCommand("points", "gives free points, or fixed points in a stat if you name it; negative takes them away (admins)", PointsCommand::node);

	private static LiteralArgumentBuilder<CommandSourceStack> node(LiteralArgumentBuilder<CommandSourceStack> literal) {
		RequiredArgumentBuilder<CommandSourceStack, Integer> amount = Commands.argument("amount", IntegerArgumentType.integer())
				.executes(command -> give(command, SoulsStats::giveFreePoints));
		for (Stat stat : Stat.REGISTRY) {
			amount.then(Commands.literal(stat.id().getPath())
					.executes(command -> give(command, (player, points) -> SoulsStats.giveBonus(player, stat, points))));
		}
		return literal.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.argument("players", EntityArgument.players()).then(amount));
	}

	private static int give(CommandContext<CommandSourceStack> command, ObjIntConsumer<ServerPlayer> give) throws CommandSyntaxException {
		int amount = IntegerArgumentType.getInteger(command, "amount");
		Collection<ServerPlayer> players = EntityArgument.getPlayers(command, "players");
		players.forEach(player -> give.accept(player, amount));
		command.getSource().sendSuccess(() -> Text.tr("points_given", "%s points to %s player(s)", amount, players.size()).withStyle(ChatFormatting.GRAY), true);
		return players.size();
	}
}
