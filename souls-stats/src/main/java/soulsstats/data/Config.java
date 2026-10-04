package soulsstats.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import soulsstats.stat.Stat;

/**
 * Los números de las stats y la curva de nivel, desde `data/soulsstats/config.json`. Los puntos de cada efecto son los
 * de su stat sobre la base; en level_xp, el nivel actual.
 */
// @decision ADR-0006: valores globales en config.json, cada efecto como escala lineal; un datapack pisa campos.
public record Config(Scaling levelXp, Scaling vigorHealth, Scaling vigorLoad, Scaling dexterityAttackSpeed, Scaling dexterityMovement,
		Scaling luckCriticalChance, float criticalDamage, Scaling weaponScaling, Map<Stat, Float> defaultWeaponScaling,
		float unmetRequirementDamage, Map<ScalingGrade, Float> scalingGrades) {
	static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
			Scaling.CODEC.fieldOf("level_xp").forGetter(Config::levelXp),
			Scaling.CODEC.fieldOf("vigor_health").forGetter(Config::vigorHealth),
			Scaling.CODEC.fieldOf("vigor_load").forGetter(Config::vigorLoad),
			Scaling.CODEC.fieldOf("dexterity_attack_speed").forGetter(Config::dexterityAttackSpeed),
			Scaling.CODEC.fieldOf("dexterity_movement").forGetter(Config::dexterityMovement),
			Scaling.CODEC.fieldOf("luck_critical_chance").forGetter(Config::luckCriticalChance),
			Codec.FLOAT.fieldOf("critical_damage").forGetter(Config::criticalDamage),
			Scaling.CODEC.fieldOf("weapon_scaling").forGetter(Config::weaponScaling),
			Stat.mapCodec(Codec.FLOAT).fieldOf("default_weapon_scaling").forGetter(Config::defaultWeaponScaling),
			Codec.FLOAT.fieldOf("unmet_requirement_damage").forGetter(Config::unmetRequirementDamage),
			Codec.unboundedMap(ScalingGrade.CODEC, Codec.FLOAT).fieldOf("scaling_grades").forGetter(Config::scalingGrades))
			.apply(i, Config::new));
	/** El cliente lo necesita para las letras de escalado del tooltip. */
	static final StreamCodec<ByteBuf, Config> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
	private static Config current;

	public static void register() {
		Datapack.config("config.json", CODEC, loaded -> current = loaded);
		ClientSync.register(ConfigPayload.TYPE, ConfigPayload.CODEC, () -> new ConfigPayload(current));
	}

	/** En el cliente: el config que mandó el servidor. */
	public static void receive(Config received) {
		current = received;
	}

	/** El config.json del mod siempre carga antes que cualquier jugador; un datapack inválido deja el anterior. */
	public static Config get() {
		return current;
	}
}
