package soulsstats.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.display.Text;

/** `/soulsstats`: muestra nivel, XP, puntos libres y stats del jugador. Registra los subcomandos de esta carpeta. */
public final class SoulsStatsCommand {
	static final String NAME = "soulsstats", DESCRIPTION = "your level, XP, free points and stats";
	/** El registro de subcomandos: se registran en este orden y help los lista igual. */
	static final List<SubCommand> SUBCOMMANDS = List.of(HelpCommand.COMMAND, RaiseCommand.COMMAND, WeightCommand.COMMAND,
			PointsCommand.COMMAND, LevelsCommand.COMMAND);

	public static void register() {
		// Corre al iniciar el servidor, con las stats que todos los mods ya registraron.
		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
			LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(NAME).executes(command -> {
				ServerPlayer player = command.getSource().getPlayerOrException();
				command.getSource().sendSuccess(() -> Text.summary(player), false);
				return 1;
			});
			SUBCOMMANDS.forEach(sub -> root.then(sub.build().apply(Commands.literal(sub.name()))));
			dispatcher.register(root);
		});
	}
}
