package soulsstats.data;

import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;

/** Manda datos del servidor al cliente. Un cliente sin el mod no recibe nada. */
public final class ClientSync {
	/** Lo mismo para todos (tablas, config): al entrar y después de cada /reload. */
	public static <T extends CustomPacketPayload> void register(Type<T> type, StreamCodec<ByteBuf, T> codec, Supplier<T> payload) {
		type(type, codec);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> send(handler.player, payload.get()));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) ->
				PlayerLookup.all(server).forEach(player -> send(player, payload.get())));
	}

	/** Para datos de cada jugador, que se mandan con send cuando cambian. */
	public static <T extends CustomPacketPayload> void type(Type<T> type, StreamCodec<ByteBuf, T> codec) {
		PayloadTypeRegistry.clientboundPlay().register(type, codec);
	}

	public static void send(ServerPlayer player, CustomPacketPayload payload) {
		if (ServerPlayNetworking.canSend(player, payload.type())) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
