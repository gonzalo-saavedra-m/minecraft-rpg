package soulsstats.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.LoggerFactory;

/**
 * Lee `data/soulsstats/<archivo>` de todos los datapacks, de menor a mayor prioridad, al iniciar y con /reload, y le
 * entrega el resultado mezclado a onLoad. Las entradas inválidas se reportan en el log y se ignoran.
 */
public final class Datapack {
	/** Tabla id → entrada: cada datapack pisa solo las entradas que trae, enteras. */
	public static <V> void table(String file, Codec<V> entry, Consumer<Map<Identifier, V>> onLoad) {
		Codec<Map<Identifier, V>> codec = Codec.unboundedMap(Identifier.CODEC, entry);
		listen(file, sources -> {
			Map<Identifier, V> loaded = new HashMap<>();
			sources.forEach((pack, json) -> codec.parse(JsonOps.INSTANCE, json).resultOrPartial(error -> log(file, pack, error))
					.ifPresent(loaded::putAll));
			onLoad.accept(Map.copyOf(loaded));
		});
	}

	/**
	 * Tabla id → entrada mezclada campo a campo: cada datapack pisa solo los campos que trae, también dentro de
	 * objetos (stats, escalados). Una entrada inválida se descarta sola.
	 */
	public static <V> void fieldTable(String file, Codec<V> entry, Consumer<Map<Identifier, V>> onLoad) {
		listen(file, sources -> {
			Map<Identifier, V> loaded = new HashMap<>();
			merge(file, sources).entrySet().forEach(item -> Identifier.read(item.getKey())
					.flatMap(id -> entry.parse(JsonOps.INSTANCE, item.getValue()).map(value -> Map.entry(id, value)))
					.resultOrPartial(error -> log(file, item.getKey(), error)).ifPresent(parsed -> loaded.put(parsed.getKey(), parsed.getValue())));
			onLoad.accept(Map.copyOf(loaded));
		});
	}

	/** Un objeto mezclado campo a campo, como fieldTable. Si la mezcla queda inválida, se mantiene la anterior. */
	public static <V> void config(String file, Codec<V> codec, Consumer<V> onLoad) {
		listen(file, sources -> codec.parse(JsonOps.INSTANCE, merge(file, sources))
				.resultOrPartial(error -> log(file, "la mezcla", error)).ifPresent(onLoad));
	}

	private static JsonObject merge(String file, Map<String, JsonElement> sources) {
		JsonObject merged = new JsonObject();
		sources.forEach((pack, json) -> merge(file, pack, "", merged, json.getAsJsonObject()));
		return merged;
	}

	/** Objetos campo a campo; el resto (números, listas) se reemplaza, y el log dice quién pisó qué. */
	private static void merge(String file, String pack, String path, JsonObject target, JsonObject source) {
		for (Map.Entry<String, JsonElement> field : source.entrySet()) {
			JsonElement old = target.get(field.getKey());
			if (old != null && old.isJsonObject() && field.getValue().isJsonObject()) {
				merge(file, pack, path + field.getKey() + ".", old.getAsJsonObject(), field.getValue().getAsJsonObject());
				continue;
			}
			if (old != null && !old.equals(field.getValue())) {
				LoggerFactory.getLogger("soulsstats").info("{}: {} pisa {}{} = {} con {}", file, pack, path, field.getKey(), old, field.getValue());
			}
			target.add(field.getKey(), field.getValue().deepCopy());
		}
	}

	/** sources: el JSON de cada datapack por su id, en orden de prioridad. */
	private static void listen(String file, Consumer<Map<String, JsonElement>> onLoad) {
		Identifier id = Identifier.fromNamespaceAndPath("soulsstats", file);
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, (ResourceManagerReloadListener) manager -> {
			Map<String, JsonElement> sources = new LinkedHashMap<>();
			for (Resource resource : manager.getResourceStack(id)) {
				try (Reader reader = resource.openAsReader()) {
					sources.put(resource.sourcePackId(), JsonParser.parseReader(reader));
				} catch (IOException | JsonParseException e) {
					log(file, resource.sourcePackId(), e.getMessage());
				}
			}
			onLoad.accept(sources);
		});
	}

	private static void log(String file, String pack, String error) {
		LoggerFactory.getLogger("soulsstats").error("{} en {}: {}", file, pack, error);
	}
}
