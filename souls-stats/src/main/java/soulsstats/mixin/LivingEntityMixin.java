package soulsstats.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import soulsstats.stat.CriticalDamage;

/** El crítico propio cambia el daño antes de que Minecraft lo procese (Fabric API no tiene un evento para eso). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
	private float soulsstats$critical(float amount, @Local(argsOnly = true) DamageSource source) {
		return CriticalDamage.apply((LivingEntity) (Object) this, source, amount);
	}
}
