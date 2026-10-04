package soulsstats.stat;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import soulsstats.data.Config;
import soulsstats.event.PlayerRefresh;

/** Vigor → vida máxima: suma vigor_health (config.json) según los puntos sobre la base. */
public final class VigorHealth {
	private static final Identifier MODIFIER = Identifier.fromNamespaceAndPath("soulsstats", "vigor");

	public static void register() {
		PlayerRefresh.register(VigorHealth::apply);
	}

	private static void apply(ServerPlayer player) {
		player.getAttribute(Attributes.MAX_HEALTH).addOrReplacePermanentModifier(
				new AttributeModifier(MODIFIER, Config.get().vigorHealth().at(Stat.VIGOR.points(player)), AttributeModifier.Operation.ADD_VALUE));
	}
}
