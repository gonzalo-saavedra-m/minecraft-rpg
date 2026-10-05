package soulsstats.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.display.Text;
import soulsstats.stat.VigorLoad;
import soulsstats.weight.ItemWeight;
import soulsstats.weight.Load;

/** `/soulsstats weight`: muestra el peso equipado, la carga máxima y el nivel de carga del jugador. */
final class WeightCommand {
	static final SubCommand COMMAND = new SubCommand("weight", "your equipped weight, max load and load level", WeightCommand::node);

	private static LiteralArgumentBuilder<CommandSourceStack> node(LiteralArgumentBuilder<CommandSourceStack> literal) {
		return literal.executes(command -> {
			ServerPlayer player = command.getSource().getPlayerOrException();
			float weight = ItemWeight.carried(player), max = VigorLoad.max(player);
			command.getSource().sendSuccess(() -> Text.tr("weight", "Weight %s / %s · %s", Text.gold(weight), Text.gold(max),
					Text.load(Load.of(weight, max))).withStyle(ChatFormatting.GRAY), false);
			return 1;
		});
	}
}
