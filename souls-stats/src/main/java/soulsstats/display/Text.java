package soulsstats.display;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import soulsstats.SoulsStats;
import soulsstats.progress.PlayerProgress;
import soulsstats.stat.Stat;

/**
 * Texto coloreado que comparten comandos y avisos (ADR-0005 del mod). Todo lo que se lee va por tr: se traduce con
 * assets/soulsstats/lang en el cliente, y en inglés si el cliente no tiene el mod (ADR-0009 de la raíz).
 */
public final class Text {
	/** La clave "soulsstats.<key>" en el idioma del jugador; english si falta o el cliente no tiene el mod. */
	public static MutableComponent tr(String key, String english, Object... args) {
		return Component.translatableWithFallback("soulsstats." + key, english, args);
	}

	/** Nivel, XP, puntos libres y una línea con todas las stats, contando las de los ítems equipados. */
	public static MutableComponent summary(ServerPlayer player) {
		PlayerProgress progress = SoulsStats.progress(player);
		MutableComponent text = tr("summary", "Level %s · XP %s", gold(progress.level()),
				Component.literal(progress.xp() + "/" + progress.nextLevelXp()).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY)
				.append(freePoints(progress)).append("\n");
		String separator = "";
		for (Stat stat : Stat.REGISTRY) {
			text.append(gray(separator)).append(name(stat).withStyle(ChatFormatting.GRAY)).append(" ").append(gold(SoulsStats.stat(player, stat)));
			separator = " · ";
		}
		return text;
	}

	/** Nombre visible de una stat: la clave "stat.<namespace>.<id>" traducida o, si falta, su id con mayúscula. */
	public static MutableComponent name(Stat stat) {
		String path = stat.id().getPath();
		return Component.translatableWithFallback("stat." + stat.id().getNamespace() + "." + path,
				path.substring(0, 1).toUpperCase() + path.substring(1));
	}

	/** " · N free points" en verde, o nada si no hay. */
	public static MutableComponent freePoints(PlayerProgress progress) {
		return progress.freePoints() == 0 ? Component.empty()
				: gray(" · ").append(tr("free_points", "%s free points (/soulsstats raise)", progress.freePoints()).withStyle(ChatFormatting.GREEN));
	}

	public static MutableComponent gray(String text) {
		return Component.literal(text).withStyle(ChatFormatting.GRAY);
	}

	public static MutableComponent gold(int value) {
		return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GOLD);
	}

	public static MutableComponent gold(float value) {
		return Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value)).withStyle(ChatFormatting.GOLD);
	}
}
