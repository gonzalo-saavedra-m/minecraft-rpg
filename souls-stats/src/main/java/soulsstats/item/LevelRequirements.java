package soulsstats.item;

import java.util.Optional;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import soulsstats.SoulsStats;
import soulsstats.display.Notifications;
import soulsstats.event.PlayerRefresh;

/**
 * Topes por nivel de items.json: bajo usable_at_level no se golpea con el arma (el golpe se cancela); bajo
 * equipable_at_level una pieza de armadura no se queda puesta y vuelve al inventario (o cae, si está lleno). Se revisa
 * en cada cambio de equipo, así no importa cómo se equipó.
 */
public final class LevelRequirements {
	public static void register() {
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> SoulsStats.player(player)
				.flatMap(server -> missing(server, ItemProperties.of(server.getItemInHand(hand)).usableAtLevel()).<InteractionResult>map(required -> {
					Notifications.cannotUse(server, required);
					return InteractionResult.FAIL;
				})).orElse(InteractionResult.PASS));
		PlayerRefresh.register(LevelRequirements::unequip);
	}

	private static void unequip(ServerPlayer player) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			ItemStack stack = player.getItemBySlot(slot);
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
				missing(player, ItemProperties.of(stack).equipableAtLevel()).ifPresent(required -> {
					player.setItemSlot(slot, ItemStack.EMPTY);
					player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
					Notifications.cannotEquip(player, required);
				});
			}
		}
	}

	/** El nivel que pide el ítem, si el jugador no lo tiene. */
	private static Optional<Integer> missing(ServerPlayer player, Optional<Integer> required) {
		return required.filter(level -> SoulsStats.progress(player).level() < level);
	}
}
