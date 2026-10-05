package soulsstats.progress;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.SoulsStats;
import soulsstats.data.ClientSync;
import soulsstats.event.PlayerRefresh;
import soulsstats.stat.Stat;
import soulsstats.stat.VigorLoad;
import soulsstats.weight.ItemWeight;

/**
 * Progreso, stats (contando sus ítems equipados) y carga de un jugador, del servidor a su cliente, cada vez que pueden
 * cambiar: los muestra la pantalla de stats y el tooltip marca en rojo los requisitos que no cumple.
 */
public record PlayerStatsPayload(int level, int xp, int nextLevelXp, int freePoints, Map<Stat, Integer> stats, float weight, float maxLoad)
		implements CustomPacketPayload {
	public static final Type<PlayerStatsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("soulsstats", "player_stats"));
	private static final StreamCodec<ByteBuf, PlayerStatsPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PlayerStatsPayload::level, ByteBufCodecs.VAR_INT, PlayerStatsPayload::xp,
			ByteBufCodecs.VAR_INT, PlayerStatsPayload::nextLevelXp, ByteBufCodecs.VAR_INT, PlayerStatsPayload::freePoints,
			Stat.POINTS_STREAM_CODEC, PlayerStatsPayload::stats, ByteBufCodecs.FLOAT, PlayerStatsPayload::weight,
			ByteBufCodecs.FLOAT, PlayerStatsPayload::maxLoad, PlayerStatsPayload::new);
	private static PlayerStatsPayload client;

	public static void register() {
		ClientSync.type(TYPE, CODEC);
		PlayerRefresh.register(PlayerStatsPayload::send);
	}

	/** En el cliente: lo último que mandó el servidor, o null al desconectarse. */
	public static void receive(PlayerStatsPayload received) {
		client = received;
	}

	/** En el cliente: vacío si el servidor no tiene el mod. */
	public static Optional<PlayerStatsPayload> client() {
		return Optional.ofNullable(client);
	}

	/** Además de PlayerRefresh, lo llama quien cambia la XP: ganarla sin subir de nivel no refresca nada más. */
	public static void send(ServerPlayer player) {
		PlayerProgress progress = SoulsStats.progress(player);
		ClientSync.send(player, new PlayerStatsPayload(progress.level(), progress.xp(), progress.nextLevelXp(), progress.freePoints(),
				Stat.REGISTRY.stream().collect(Collectors.toMap(Function.identity(), stat -> SoulsStats.stat(player, stat))),
				ItemWeight.carried(player), VigorLoad.max(player)));
	}

	@Override
	public Type<PlayerStatsPayload> type() {
		return TYPE;
	}
}
