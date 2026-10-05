package soulsstats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import soulsstats.command.SoulsStatsCommand;
import soulsstats.data.Config;
import soulsstats.display.Notifications;
import soulsstats.event.LevelUpEvent;
import soulsstats.event.StatsChangedEvent;
import soulsstats.progress.MobXpTable;
import soulsstats.progress.PlayerProgress;
import soulsstats.stat.RespecTotemItem;
import soulsstats.stat.Stat;
import soulsstats.stat.DexteritySpeed;
import soulsstats.stat.VigorHealth;
import soulsstats.stat.WeaponScaling;
import soulsstats.item.ItemProperties;
import soulsstats.item.LevelRequirements;
import soulsstats.progress.PlayerStatsPayload;
import soulsstats.progress.RaisePayload;
import soulsstats.weight.Load;

public class SoulsStats implements ModInitializer {
	private static final AttachmentType<PlayerProgress> PROGRESS = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath("soulsstats", "progress"),
			builder -> builder.persistent(PlayerProgress.CODEC).initializer(() -> PlayerProgress.START).copyOnDeath());
	private static final List<Function<LivingEntity, List<ItemStack>>> EQUIPMENT = new ArrayList<>();

	@Override
	public void onInitialize() {
		Identifier respec = Identifier.fromNamespaceAndPath("soulsstats", "respec");
		Registry.register(BuiltInRegistries.ITEM, respec,
				new RespecTotemItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, respec))));
		Config.register();
		MobXpTable.register();
		ItemProperties.register();
		LevelRequirements.register();
		PlayerStatsPayload.register();
		RaisePayload.register();
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((level, killer, killed, source) ->
				player(killer).ifPresent(player -> {
					PlayerProgress before = progress(player);
					setProgress(player, before, before.gain(MobXpTable.of(killed, before.level())));
				}));
		VigorHealth.register();
		WeaponScaling.register();
		DexteritySpeed.register();
		Load.register();
		Notifications.register();
		SoulsStatsCommand.register();
	}

	public static PlayerProgress progress(ServerPlayer player) {
		return player.getAttachedOrCreate(PROGRESS);
	}

	/** Reparte puntos libres en una stat; false si no alcanzan. */
	public static boolean raise(ServerPlayer player, Stat stat, int amount) {
		PlayerProgress progress = progress(player);
		if (amount > progress.freePoints()) {
			return false;
		}
		changeStats(player, progress.raise(stat, amount));
		return true;
	}

	/** Niveles a mano (comprados, de recompensa); negativo los quita, sin bajar de 1. */
	public static void giveLevels(ServerPlayer player, int amount) {
		PlayerProgress before = progress(player);
		setProgress(player, before, before.giveLevels(amount));
	}

	private static void setProgress(ServerPlayer player, PlayerProgress before, PlayerProgress after) {
		player.setAttached(PROGRESS, after);
		if (after.level() > before.level()) {
			new LevelUpEvent(player, after).emit();
		} else if (after.level() < before.level()) {
			// Bajar de nivel (por comando) puede dejar puesta armadura que ya no se puede equipar.
			new StatsChangedEvent(player, after).emit();
		}
		PlayerStatsPayload.send(player);
	}

	/** Puntos libres que no vienen del nivel (comprados, de recompensa); negativo los quita. */
	public static void giveFreePoints(ServerPlayer player, int amount) {
		changeStats(player, progress(player).giveFreePoints(amount));
	}

	/** Puntos fijos en una stat que el respec no devuelve, como los de una clase; negativo la baja. */
	public static void giveBonus(ServerPlayer player, Stat stat, int amount) {
		changeStats(player, progress(player).giveBonus(stat, amount));
	}

	/** Valor de la stat: el de su progreso más lo que suman sus ítems equipados (stats de items.json). */
	public static int stat(ServerPlayer player, Stat stat) {
		return progress(player).stat(stat) + equipped(player).mapToInt(item -> ItemProperties.of(item).stats().getOrDefault(stat, 0)).sum();
	}

	/** Para mods puente con mods de accesorios: suma ítems equipados (anillos, amuletos) a armadura y manos. */
	public static void addEquipment(Function<LivingEntity, List<ItemStack>> source) { // adr-skip ADR-0003: API pública para mods puente
		EQUIPMENT.add(source);
	}

	public static Stream<ItemStack> equipped(LivingEntity entity) {
		return Stream.concat(Arrays.stream(EquipmentSlot.values()).map(entity::getItemBySlot),
				EQUIPMENT.stream().flatMap(source -> source.apply(entity).stream()));
	}

	/** Única entrada para cambiar stats: guarda y avisa a quien escuche (vigor, display). */
	public static void changeStats(ServerPlayer player, PlayerProgress progress) {
		player.setAttached(PROGRESS, progress);
		new StatsChangedEvent(player, progress).emit();
	}

	public static Optional<ServerPlayer> player(Entity entity) {
		return entity instanceof ServerPlayer player ? Optional.of(player) : Optional.empty(); // adr-skip ADR-0006: proxy, un evento o un ítem entrega una entidad que puede ser jugador del servidor
	}
}
