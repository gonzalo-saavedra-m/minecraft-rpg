package soulsstats.event;

import net.minecraft.server.level.ServerPlayer;
import soulsstats.progress.PlayerProgress;

/** Un jugador subió uno o más niveles; progress es su estado nuevo. */
public record LevelUpEvent(ServerPlayer player, PlayerProgress progress) implements EventBus.Event {
	@Override
	public void emit() {
		EventBus.emit(LevelUpEvent.class, this);
	}
}
