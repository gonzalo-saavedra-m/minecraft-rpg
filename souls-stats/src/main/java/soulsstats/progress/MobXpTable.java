package soulsstats.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import soulsstats.data.Datapack;

// @decision ADR-0001: XP por tipo de mob, desde `data/soulsstats/mob_xp.json` de todos los datapacks.
public final class MobXpTable {
	private static Map<Identifier, Entry> table = Map.of();

	/** maxLevel es el nivel más alto al que lleva ese mob: desde ahí no da XP. */
	private record Entry(int xp, int maxLevel) {
		static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
				ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xp").forGetter(Entry::xp),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_level", Integer.MAX_VALUE).forGetter(Entry::maxLevel))
				.apply(i, Entry::new));
	}

	public static void register() {
		Datapack.table("mob_xp.json", Entry.CODEC, loaded -> table = loaded);
	}

	/** XP que da killed a un jugador de ese nivel: 0 si el mob no está en la tabla o el jugador ya pasó su tope. */
	public static int of(LivingEntity killed, int level) {
		Entry entry = table.get(BuiltInRegistries.ENTITY_TYPE.getKey(killed.getType()));
		return entry == null || level >= entry.maxLevel() ? 0 : entry.xp();
	}
}
