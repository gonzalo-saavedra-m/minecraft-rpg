package soulsstats.event;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.SoulsStats;

/** Recalcula algo del jugador cada vez que sus stats, su equipo, sus números (/reload) o sus efectos pueden haber cambiado. */
public final class PlayerRefresh {
	public static void register(Consumer<ServerPlayer> refresh) {
		EventBus.listen(StatsChangedEvent.class, event -> refresh.accept(event.player()));
		// Cambiar equipo (también el ítem en la mano) cambia el peso y las stats que dan los ítems.
		ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, current) -> SoulsStats.player(entity).ifPresent(refresh));
		// Al reaparecer tras morir, Minecraft no copia los modificadores del jugador anterior.
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> refresh.accept(newPlayer));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> refresh.accept(handler.player));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> PlayerLookup.all(server).forEach(refresh));
	}
}
