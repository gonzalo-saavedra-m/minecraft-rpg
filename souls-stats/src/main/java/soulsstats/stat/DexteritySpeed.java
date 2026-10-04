package soulsstats.stat;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import soulsstats.data.Config;
import soulsstats.event.PlayerRefresh;

/**
 * Destreza → velocidad: multiplica la velocidad de ataque por (1 + dexterity_attack_speed) y, de a poco, la de
 * movimiento por (1 + dexterity_movement), según los puntos sobre la base (config.json). La de movimiento la aplica
 * weight/Load junto con la carga, porque un modificador de atributo haría el zoom de poción.
 */
public final class DexteritySpeed {
	private static final Identifier MODIFIER = Identifier.fromNamespaceAndPath("soulsstats", "dexterity");

	public static void register() {
		PlayerRefresh.register(DexteritySpeed::apply);
	}

	public static float movement(ServerPlayer player) {
		return 1 + Config.get().dexterityMovement().at(Stat.DEXTERITY.points(player));
	}

	private static void apply(ServerPlayer player) {
		player.getAttribute(Attributes.ATTACK_SPEED).addOrReplacePermanentModifier(new AttributeModifier(
				MODIFIER, Config.get().dexterityAttackSpeed().at(Stat.DEXTERITY.points(player)), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}
}
