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
import soulsstats.event.EventBus;
import soulsstats.event.LevelUpEvent;
import soulsstats.event.PlayerRefresh;
import soulsstats.stat.Stat;

/**
 * Nivel y stats de un jugador (contando sus ítems equipados), del servidor a su cliente, cada vez que pueden cambiar:
 * el tooltip marca en rojo los requisitos que no cumple.
 */
public record PlayerStatsPayload(int level, Map<Stat, Integer> stats) implements CustomPacketPayload {
	public static final Type<PlayerStatsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("soulsstats", "player_stats"));
	private static final StreamCodec<ByteBuf, PlayerStatsPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PlayerStatsPayload::level, Stat.POINTS_STREAM_CODEC, PlayerStatsPayload::stats, PlayerStatsPayload::new);
	private static PlayerStatsPayload client;

	public static void register() {
		ClientSync.type(TYPE, CODEC);
		PlayerRefresh.register(PlayerStatsPayload::send);
		EventBus.listen(LevelUpEvent.class, event -> send(event.player()));
	}

	/** En el cliente: lo último que mandó el servidor, o null al desconectarse. */
	public static void receive(PlayerStatsPayload received) {
		client = received;
	}

	/** En el cliente: vacío si el servidor no tiene el mod. */
	public static Optional<PlayerStatsPayload> client() {
		return Optional.ofNullable(client);
	}

	private static void send(ServerPlayer player) {
		ClientSync.send(player, new PlayerStatsPayload(SoulsStats.progress(player).level(),
				Stat.REGISTRY.stream().collect(Collectors.toMap(Function.identity(), stat -> SoulsStats.stat(player, stat)))));
	}

	@Override
	public Type<PlayerStatsPayload> type() {
		return TYPE;
	}
}
