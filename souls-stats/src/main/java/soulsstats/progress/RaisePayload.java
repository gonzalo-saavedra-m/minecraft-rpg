package soulsstats.progress;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import soulsstats.SoulsStats;
import soulsstats.stat.Stat;

/** Del cliente al servidor: el botón + de la pantalla de stats reparte puntos libres, como /soulsstats raise. */
public record RaisePayload(Identifier stat, int amount) implements CustomPacketPayload {
	public static final Type<RaisePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("soulsstats", "raise"));
	private static final StreamCodec<ByteBuf, RaisePayload> CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, RaisePayload::stat, ByteBufCodecs.VAR_INT, RaisePayload::amount, RaisePayload::new);

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
		// El cliente no es de fiar: una stat que no existe se ignora y una cantidad bajo 1 restaría puntos.
		ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> Optional.ofNullable(Stat.REGISTRY.getValue(payload.stat()))
				.filter(stat -> payload.amount() > 0).ifPresent(stat -> SoulsStats.raise(context.player(), stat, payload.amount())));
	}

	@Override
	public Type<RaisePayload> type() {
		return TYPE;
	}
}
