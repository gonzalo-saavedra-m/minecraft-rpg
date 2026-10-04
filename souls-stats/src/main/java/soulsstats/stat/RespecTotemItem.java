package soulsstats.stat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import soulsstats.SoulsStats;

/** Consumible que devuelve todos los puntos repartidos. Sin receta: los admins deciden cómo se obtiene. */
public final class RespecTotemItem extends Item {
	public RespecTotemItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		return SoulsStats.player(player).filter(server -> !SoulsStats.progress(server).points().isEmpty())
				.<InteractionResult>map(server -> {
					server.getItemInHand(hand).consume(1, server);
					SoulsStats.changeStats(server, SoulsStats.progress(server).respec());
					return InteractionResult.SUCCESS_SERVER;
				}).orElse(InteractionResult.PASS);
	}
}
