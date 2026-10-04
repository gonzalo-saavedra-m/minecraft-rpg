package soulsstats.display;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.event.EventBus;
import soulsstats.event.LevelUpEvent;
import soulsstats.event.StatsChangedEvent;

/** Avisos al jugador cuando algo le pasa sin que él lo pida: subir de nivel, cambiar stats, un ítem que pide más nivel. */
public final class Notifications {
	/** En la barra de acción: el golpe se canceló porque el arma pide más nivel (usable_at_level). */
	public static void cannotUse(ServerPlayer player, int level) {
		player.sendOverlayMessage(Text.tr("cannot_use", "You need level %s to use this weapon", level).withStyle(ChatFormatting.RED));
	}

	/** En la barra de acción: la pieza volvió al inventario porque pide más nivel (equipable_at_level). */
	public static void cannotEquip(ServerPlayer player, int level) {
		player.sendOverlayMessage(Text.tr("cannot_equip", "You need level %s to equip this", level).withStyle(ChatFormatting.RED));
	}

	public static void register() {
		EventBus.listen(LevelUpEvent.class, event -> event.player().sendSystemMessage(
				Text.tr("level_up", "You reached level %s", event.progress().level()).withStyle(ChatFormatting.GOLD)
						.append(Text.freePoints(event.progress()))));
		EventBus.listen(StatsChangedEvent.class, event -> event.player().sendSystemMessage(Text.summary(event.player())));
	}
}
