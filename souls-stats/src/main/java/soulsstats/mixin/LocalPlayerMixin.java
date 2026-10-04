package soulsstats.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import soulsstats.weight.Load;

/** Sobrecargado no se corre. El cliente decide si corre, así que esto solo rige si el cliente tiene el mod. */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	/** Minecraft pregunta aquí tanto para empezar a correr como para seguir corriendo. */
	@Inject(method = "isSprintingPossible", at = @At("HEAD"), cancellable = true)
	private void soulsstats$overloaded(boolean flying, CallbackInfoReturnable<Boolean> info) {
		if (Load.overloaded((LocalPlayer) (Object) this)) {
			info.setReturnValue(false);
		}
	}
}
