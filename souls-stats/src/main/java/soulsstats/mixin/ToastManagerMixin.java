package soulsstats.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soulsstats.display.LevelUpToast;

/** Guarda el ToastManager al crearse: su constructor es igual en todas las versiones, su getter no (ADR-0010 de la raíz). */
@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void soulsstats$capture(Minecraft minecraft, Options options, CallbackInfo info) {
		LevelUpToast.use((ToastManager) (Object) this);
	}
}
