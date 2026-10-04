package soulsstats.weight;

import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import soulsstats.SoulsStats;
import soulsstats.item.ItemProperties;

/** Peso de un ítem y de lo que lleva equipado un jugador. */
public final class ItemWeight {
	/** Peso de lo que lleva equipado (SoulsStats.equipped); el resto del inventario no pesa. */
	public static float carried(LivingEntity entity) {
		return (float) SoulsStats.equipped(entity).mapToDouble(ItemWeight::of).sum();
	}

	// ponytail: fórmula provisional; un arma pesa por su daño por golpe, no por su DPS (un hacha más que una espada).
	/** Peso de items.json o, sin peso ahí, lo que el ítem suma en armadura, dureza y daño de ataque (0 si nada). */
	public static float of(ItemStack stack) {
		return ItemProperties.of(stack).weight().orElseGet(() -> (float) stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
				.modifiers().stream()
				.filter(modifier -> Set.of(Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.ATTACK_DAMAGE).contains(modifier.attribute())
						&& modifier.modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
				.mapToDouble(modifier -> modifier.modifier().amount()).sum());
	}
}
