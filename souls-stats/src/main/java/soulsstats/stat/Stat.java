package soulsstats.stat;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import soulsstats.SoulsStats;

/**
 * Stat que se sube con puntos de nivel, desde base. Otros mods suman las suyas con Registry.register en REGISTRY
 * (ADR-0003 del mod); el path del id es el nombre en comandos. Clase y no record: cada stat es única por identidad.
 */
// @decision ADR-0003: stats en un registro de Minecraft con las propias por defecto; se guardan los puntos por id.
public final class Stat {
	public static final Registry<Stat> REGISTRY = FabricRegistryBuilder.create(
			ResourceKey.<Stat>createRegistryKey(Identifier.fromNamespaceAndPath("soulsstats", "stat"))).buildAndRegister();
	public static final Stat VIGOR = own("vigor", 10), STRENGTH = own("strength", 10),
			DEXTERITY = own("dexterity", 10), LUCK = own("luck", 10);

	/** Id de stat en JSON: sin namespace es de este mod ("vigor"); las de otros mods llevan el suyo ("magic:fe"). */
	private static final Codec<Identifier> ID_CODEC = Codec.STRING.comapFlatMap(
			id -> Identifier.read(id.contains(":") ? id : "soulsstats:" + id), Identifier::toString);
	/** Puntos por stat; al cargar se descartan los de stats que ya no existen (en el jugador, vuelven a estar libres). */
	public static final Codec<Map<Stat, Integer>> POINTS_CODEC = mapCodec(Codec.INT);
	/** Lo mismo por red: el cliente descarta las stats que no conoce. */
	public static final StreamCodec<ByteBuf, Map<Stat, Integer>> POINTS_STREAM_CODEC = mapStreamCodec(ByteBufCodecs.VAR_INT);

	public final int base;

	public Stat(int base) {
		this.base = base;
	}

	/** Puntos del jugador sobre la base, contando los de sus ítems equipados: lo que usan los efectos. */
	public int points(ServerPlayer player) {
		return SoulsStats.stat(player, this) - base;
	}

	public Identifier id() {
		return REGISTRY.getKey(this);
	}

	/** Un valor por stat en JSON; las stats que no existen se descartan. */
	public static <V> Codec<Map<Stat, V>> mapCodec(Codec<V> value) {
		return Codec.unboundedMap(ID_CODEC, value).xmap(Stat::known, Stat::byId);
	}

	public static <V> StreamCodec<ByteBuf, Map<Stat, V>> mapStreamCodec(StreamCodec<? super ByteBuf, V> value) {
		return ByteBufCodecs.<ByteBuf, Identifier, V, Map<Identifier, V>>map(HashMap::new, Identifier.STREAM_CODEC, value).map(Stat::known, Stat::byId);
	}

	private static <V> Map<Stat, V> known(Map<Identifier, V> byId) {
		return byId.entrySet().stream().filter(entry -> REGISTRY.containsKey(entry.getKey()))
				.collect(Collectors.toUnmodifiableMap(entry -> REGISTRY.getValue(entry.getKey()), Map.Entry::getValue));
	}

	private static <V> Map<Identifier, V> byId(Map<Stat, V> byStat) {
		return byStat.entrySet().stream().collect(Collectors.toMap(entry -> REGISTRY.getKey(entry.getKey()), Map.Entry::getValue));
	}

	private static Stat own(String name, int base) {
		return Registry.register(REGISTRY, Identifier.fromNamespaceAndPath("soulsstats", name), new Stat(base));
	}
}
