package soulsstats.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import soulsstats.data.ClientSync;
import soulsstats.data.Config;
import soulsstats.data.Datapack;
import soulsstats.stat.Stat;

/**
 * Lo que `data/soulsstats/items.json` dice de un ítem: su peso (si no, se deriva de sus atributos), las stats que suma
 * mientras está equipado, si es arma las stats que pide (requires) y cuánto escala con cada una (scaling), y los
 * niveles desde los que se puede usar o equipar (usable_at_level, equipable_at_level; ver LevelRequirements). El
 * servidor carga la tabla y se la manda al cliente (ItemPropertiesPayload) para los tooltips.
 */
// @decision ADR-0007: propiedades por ítem desde `data/soulsstats/items.json`; sin peso, se deriva de sus atributos; se mezcla campo a campo.
public record ItemProperties(Optional<Float> weight, Map<Stat, Integer> stats, Map<Stat, Integer> requires, Map<Stat, Float> scaling,
		Optional<Integer> usableAtLevel, Optional<Integer> equipableAtLevel) {
	private static final ItemProperties NONE = new ItemProperties(Optional.empty(), Map.of(), Map.of(), Map.of(), Optional.empty(), Optional.empty());
	private static final Codec<ItemProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
			ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("weight").forGetter(ItemProperties::weight),
			Stat.POINTS_CODEC.optionalFieldOf("stats", Map.of()).forGetter(ItemProperties::stats),
			Stat.POINTS_CODEC.optionalFieldOf("requires", Map.of()).forGetter(ItemProperties::requires),
			Stat.mapCodec(Codec.FLOAT).optionalFieldOf("scaling", Map.of()).forGetter(ItemProperties::scaling),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("usable_at_level").forGetter(ItemProperties::usableAtLevel),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("equipable_at_level").forGetter(ItemProperties::equipableAtLevel))
			.apply(i, ItemProperties::new));
	static final StreamCodec<ByteBuf, ItemProperties> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(ByteBufCodecs.FLOAT), ItemProperties::weight, Stat.POINTS_STREAM_CODEC, ItemProperties::stats,
			Stat.POINTS_STREAM_CODEC, ItemProperties::requires, Stat.mapStreamCodec(ByteBufCodecs.FLOAT), ItemProperties::scaling,
			ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), ItemProperties::usableAtLevel,
			ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), ItemProperties::equipableAtLevel, ItemProperties::new);
	private static Map<Identifier, ItemProperties> table = Map.of();

	public static void register() {
		Datapack.fieldTable("items.json", CODEC, loaded -> table = loaded);
		ClientSync.register(ItemPropertiesPayload.TYPE, ItemPropertiesPayload.CODEC, () -> new ItemPropertiesPayload(table));
	}

	/** En el cliente: la tabla que mandó el servidor, o vacía al desconectarse. */
	public static void receive(Map<Identifier, ItemProperties> received) {
		table = received;
	}

	public static ItemProperties of(ItemStack stack) {
		return table.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), NONE);
	}

	/** El escalado de un arma: el de items.json o, si no trae y el ítem hace daño, default_weapon_scaling. */
	public static Map<Stat, Float> scaling(ItemStack stack) {
		Map<Stat, Float> own = of(stack).scaling();
		return own.isEmpty() && attackDamage(stack) > 0 ? Config.get().defaultWeaponScaling() : own;
	}

	/** Lo que el ítem suma al daño de ataque (su daño por golpe sobre el del puño). */
	public static double attackDamage(ItemStack stack) {
		return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers().stream()
				.filter(modifier -> modifier.attribute().equals(Attributes.ATTACK_DAMAGE)
						&& modifier.modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
				.mapToDouble(modifier -> modifier.modifier().amount()).sum();
	}
}
