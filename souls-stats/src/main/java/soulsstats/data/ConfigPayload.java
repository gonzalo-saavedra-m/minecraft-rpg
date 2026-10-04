package soulsstats.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** config.json, del servidor al cliente, para las letras de escalado del tooltip. */
public record ConfigPayload(Config config) implements CustomPacketPayload {
	public static final Type<ConfigPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("soulsstats", "config"));
	static final StreamCodec<ByteBuf, ConfigPayload> CODEC = Config.STREAM_CODEC.map(ConfigPayload::new, ConfigPayload::config);

	@Override
	public Type<ConfigPayload> type() {
		return TYPE;
	}
}
