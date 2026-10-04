package soulsstats.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Bus de eventos síncrono: guarda los listeners por clase de evento y los llama cuando un evento se emite. */
public final class EventBus {
	private static final Map<Class<?>, List<Consumer<Object>>> LISTENERS = new HashMap<>();

	/** Cada evento implementa emit() llamando a {@link EventBus#emit} con su propia clase. */
	public interface Event {
		void emit();
	}

	public static <E extends Event> void listen(Class<E> type, Consumer<E> listener) {
		LISTENERS.computeIfAbsent(type, key -> new ArrayList<>()).add(event -> listener.accept(type.cast(event)));
	}

	public static <E extends Event> void emit(Class<E> type, E event) {
		LISTENERS.getOrDefault(type, List.of()).forEach(listener -> listener.accept(event));
	}
}
