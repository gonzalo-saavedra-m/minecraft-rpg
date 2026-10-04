package soulsstats.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
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
			MutableComponent load = switch (Load.of(weight, max)) {
				case LIGHT -> Text.tr("load.light", "light load");
				case NORMAL -> Text.tr("load.normal", "normal load");
				case HEAVY -> Text.tr("load.heavy", "heavy load");
				case OVERLOADED -> Text.tr("load.overloaded", "overloaded");
			};
			command.getSource().sendSuccess(() -> Text.tr("weight", "Weight %s / %s · %s", Text.gold(weight), Text.gold(max), load)
					.withStyle(ChatFormatting.GRAY), false);
			return 1;
		});
	}
}
