package soulsstats.stat;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import soulsstats.SoulsStats;
import soulsstats.data.Config;
import soulsstats.event.PlayerRefresh;
import soulsstats.item.ItemProperties;

/**
 * Daño del arma en la mano según las stats, como en Dark Souls: suma daño del arma × Σ escalado × weapon_scaling(puntos)
 * por cada stat con la que escala (ItemProperties.scaling). Si el jugador no cumple sus requisitos (requires), no hay
 * bonus y el daño total se multiplica por unmet_requirement_damage. Todo en config.json.
 */
public final class WeaponScaling {
	private static final Identifier BONUS = Identifier.fromNamespaceAndPath("soulsstats", "weapon_scaling"),
			PENALTY = Identifier.fromNamespaceAndPath("soulsstats", "weapon_requirements");

	public static void register() {
		PlayerRefresh.register(WeaponScaling::apply);
	}

	/** Si el jugador tiene las stats que pide el arma, contando las de sus ítems equipados. */
	public static boolean meetsRequirements(ServerPlayer player, ItemStack weapon) {
		return ItemProperties.of(weapon).requires().entrySet().stream().allMatch(required -> SoulsStats.stat(player, required.getKey()) >= required.getValue());
	}

	private static void apply(ServerPlayer player) {
		ItemStack weapon = player.getMainHandItem();
		Config config = Config.get();
		boolean met = meetsRequirements(player, weapon);
		double bonus = met ? ItemProperties.attackDamage(weapon) * ItemProperties.scaling(weapon).entrySet().stream()
				.mapToDouble(scaling -> scaling.getValue() * config.weaponScaling().at(scaling.getKey().points(player))).sum() : 0;
		player.getAttribute(Attributes.ATTACK_DAMAGE).addOrReplacePermanentModifier(
				new AttributeModifier(BONUS, bonus, AttributeModifier.Operation.ADD_VALUE));
		player.getAttribute(Attributes.ATTACK_DAMAGE).addOrReplacePermanentModifier(new AttributeModifier(
				PENALTY, met ? 0 : config.unmetRequirementDamage() - 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}
}
