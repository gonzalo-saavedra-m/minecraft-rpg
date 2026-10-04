package soulsstats.event;

import net.minecraft.server.level.ServerPlayer;
import soulsstats.progress.PlayerProgress;

/** Cambiaron las stats de un jugador (subió una o se reasignaron); progress es su estado nuevo. */
public record StatsChangedEvent(ServerPlayer player, PlayerProgress progress) implements EventBus.Event {
	@Override
	public void emit() {
		EventBus.emit(StatsChangedEvent.class, this);
	}
}
