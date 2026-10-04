package soulsstats.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import soulsstats.display.Text;

/**
 * `/soulsstats help`: recorre los subcomandos registrados y muestra la sintaxis de cada uno (sacada del árbol de
 * comandos) con su descripción. Solo lista los que el jugador tiene permiso de usar.
 */
final class HelpCommand {
	static final SubCommand COMMAND = new SubCommand("help", "lists the commands and what each one does", HelpCommand::node);

	private static LiteralArgumentBuilder<CommandSourceStack> node(LiteralArgumentBuilder<CommandSourceStack> literal) {
		return literal.executes(command -> {
			CommandDispatcher<CommandSourceStack> dispatcher = command.getSource().getServer().getCommands().getDispatcher();
			CommandNode<CommandSourceStack> root = dispatcher.getRoot().getChild(SoulsStatsCommand.NAME);
			Map<CommandNode<CommandSourceStack>, String> usages = dispatcher.getSmartUsage(root, command.getSource());
			MutableComponent text = line("/" + SoulsStatsCommand.NAME, Text.tr("command.soulsstats", SoulsStatsCommand.DESCRIPTION));
			for (SubCommand sub : SoulsStatsCommand.SUBCOMMANDS) {
				String usage = usages.get(root.getChild(sub.name()));
				if (usage != null) {
					// Brigadier pone el nombre entre corchetes de opcional porque /soulsstats también se ejecuta solo.
					String syntax = usage.replaceFirst("^\\[" + sub.name() + "]", sub.name());
					text.append("\n").append(line("/" + SoulsStatsCommand.NAME + " " + syntax, Text.tr("command." + sub.name(), sub.description())));
				}
			}
			command.getSource().sendSuccess(() -> text, false);
			return 1;
		});
	}

	private static MutableComponent line(String usage, MutableComponent description) {
		return Component.literal(usage).withStyle(ChatFormatting.YELLOW).append(Text.gray(" · ")).append(description.withStyle(ChatFormatting.GRAY));
	}
}
