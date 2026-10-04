package soulsstats.stat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import soulsstats.SoulsStats;
import soulsstats.data.Config;

/**
 * Crítico propio (no el de Minecraft al golpear cayendo): cada golpe cuerpo a cuerpo de un jugador tiene probabilidad
 * luck_critical_chance según su suerte de multiplicar su daño por critical_damage. Se ve con chispas azules.
 */
public final class CriticalDamage {
	/** Lo llama el mixin de LivingEntity.hurtServer con el daño que target va a recibir. */
	public static float apply(LivingEntity target, DamageSource source, float amount) {
		if (!source.is(DamageTypes.PLAYER_ATTACK)) {
			return amount;
		}
		return SoulsStats.player(source.getEntity()).filter(CriticalDamage::roll).map(player -> {
			player.magicCrit(target);
			player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT,
					player.getSoundSource(), 1, 1);
			return amount * Config.get().criticalDamage();
		}).orElse(amount);
	}

	private static boolean roll(ServerPlayer player) {
		return player.getRandom().nextFloat() < Config.get().luckCriticalChance().at(Stat.LUCK.points(player));
	}
}
