package soulsstats;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import soulsstats.data.Config;
import soulsstats.data.ConfigPayload;
import soulsstats.display.ItemTooltip;
import soulsstats.display.LevelUpToast;
import soulsstats.display.StatsScreen;
import soulsstats.item.ItemProperties;
import soulsstats.item.ItemPropertiesPayload;
import soulsstats.progress.PlayerStatsPayload;

/** Entrada del mod en el cliente: recibe del servidor ítems, config y stats para los tooltips y la pantalla de stats (K). */
public class SoulsStatsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ItemPropertiesPayload.TYPE, (payload, context) -> ItemProperties.receive(payload.items()));
		ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) -> Config.receive(payload.config()));
		// Un servidor sin el mod no manda nada: que no queden los ítems ni las stats del anterior.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ItemProperties.receive(Map.of());
			PlayerStatsPayload.receive(null);
		});
		ItemTooltip.register();
		KeyMapping open = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.soulsstats.open", InputConstants.KEY_K,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath("soulsstats", "main"))));
		// Subir de nivel avisa con un toast; el primer dato al entrar no cuenta como subida.
		ClientPlayNetworking.registerGlobalReceiver(PlayerStatsPayload.TYPE, (payload, context) -> {
			PlayerStatsPayload.client().filter(before -> payload.level() > before.level()).ifPresent(before -> LevelUpToast.show(payload, open));
			PlayerStatsPayload.receive(payload);
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (open.consumeClick()) {
				// setScreen pasó de Minecraft a Gui en 26.2; setScreenAndShow (setScreen + un frame) está en todas (ADR-0010).
				client.setScreenAndShow(new StatsScreen(open));
			}
		});
	}
}
