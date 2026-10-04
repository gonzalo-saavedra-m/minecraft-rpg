package soulsstats.item;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** La tabla de items.json, del servidor al cliente, para los tooltips. */
public record ItemPropertiesPayload(Map<Identifier, ItemProperties> items) implements CustomPacketPayload {
	public static final Type<ItemPropertiesPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("soulsstats", "items"));
	static final StreamCodec<ByteBuf, ItemPropertiesPayload> CODEC = ByteBufCodecs.<ByteBuf, Identifier, ItemProperties, Map<Identifier, ItemProperties>>map(
			HashMap::new, Identifier.STREAM_CODEC, ItemProperties.STREAM_CODEC).map(ItemPropertiesPayload::new, ItemPropertiesPayload::items);

	@Override
	public Type<ItemPropertiesPayload> type() {
		return TYPE;
	}
}
