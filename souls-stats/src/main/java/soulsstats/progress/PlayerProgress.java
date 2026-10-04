package soulsstats.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import soulsstats.data.Config;
import soulsstats.stat.Stat;

/**
 * Nivel, XP del nivel actual (se descuenta al subir), puntos invertidos por stat, puntos libres que no vienen del
 * nivel (extraPoints) y puntos fijos por stat que el respec no devuelve (bonus, por ejemplo de una clase).
 */
public record PlayerProgress(int level, int xp, Map<Stat, Integer> points, int extraPoints, Map<Stat, Integer> bonus) {
	public static final PlayerProgress START = new PlayerProgress(1, 0, Map.of(), 0, Map.of());
	public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("level").forGetter(PlayerProgress::level),
			Codec.INT.fieldOf("xp").forGetter(PlayerProgress::xp),
			Stat.POINTS_CODEC.optionalFieldOf("points", Map.of()).forGetter(PlayerProgress::points),
			Codec.INT.optionalFieldOf("extra_points", 0).forGetter(PlayerProgress::extraPoints),
			Stat.POINTS_CODEC.optionalFieldOf("bonus", Map.of()).forGetter(PlayerProgress::bonus))
			.apply(i, PlayerProgress::new));

	public int nextLevelXp() {
		return (int) Config.get().levelXp().at(level);
	}

	public int stat(Stat stat) {
		return stat.base + bonus.getOrDefault(stat, 0) + points.getOrDefault(stat, 0);
	}

	public int freePoints() {
		return level - 1 + extraPoints - points.values().stream().mapToInt(Integer::intValue).sum();
	}

	public PlayerProgress gain(int amount) {
		PlayerProgress next = new PlayerProgress(level, xp + amount, points, extraPoints, bonus);
		while (next.xp >= next.nextLevelXp()) {
			next = new PlayerProgress(next.level + 1, next.xp - next.nextLevelXp(), points, extraPoints, bonus);
		}
		return next;
	}

	/** Quien llama revisa que amount no supere freePoints(). */
	public PlayerProgress raise(Stat stat, int amount) {
		return new PlayerProgress(level, xp, add(points, stat, amount), extraPoints, bonus);
	}

	/** Sin bajar de nivel 1; la XP se recorta si el nivel nuevo pide menos. */
	public PlayerProgress giveLevels(int amount) {
		PlayerProgress next = new PlayerProgress(Math.max(1, level + amount), xp, points, extraPoints, bonus);
		return new PlayerProgress(next.level, Math.min(xp, next.nextLevelXp() - 1), points, extraPoints, bonus);
	}

	public PlayerProgress giveFreePoints(int amount) {
		return new PlayerProgress(level, xp, points, extraPoints + amount, bonus);
	}

	public PlayerProgress giveBonus(Stat stat, int amount) {
		return new PlayerProgress(level, xp, points, extraPoints, add(bonus, stat, amount));
	}

	public PlayerProgress respec() {
		return new PlayerProgress(level, xp, Map.of(), extraPoints, bonus);
	}

	private static Map<Stat, Integer> add(Map<Stat, Integer> map, Stat stat, int amount) {
		Map<Stat, Integer> added = new HashMap<>(map);
		added.merge(stat, amount, Integer::sum);
		return Map.copyOf(added);
	}
}
