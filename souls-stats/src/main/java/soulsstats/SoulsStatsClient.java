package soulsstats;

import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import soulsstats.data.Config;
import soulsstats.data.ConfigPayload;
import soulsstats.display.ItemTooltip;
import soulsstats.item.ItemProperties;
import soulsstats.item.ItemPropertiesPayload;
import soulsstats.progress.PlayerStatsPayload;

/** Entrada del mod en el cliente: recibe la tabla de ítems y el config del servidor y los muestra en los tooltips. */
public class SoulsStatsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ItemPropertiesPayload.TYPE, (payload, context) -> ItemProperties.receive(payload.items()));
		ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) -> Config.receive(payload.config()));
		ClientPlayNetworking.registerGlobalReceiver(PlayerStatsPayload.TYPE, (payload, context) -> PlayerStatsPayload.receive(payload));
		// Un servidor sin el mod no manda nada: que no queden los ítems ni las stats del anterior.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ItemProperties.receive(Map.of());
			PlayerStatsPayload.receive(null);
		});
		ItemTooltip.register();
	}
}
