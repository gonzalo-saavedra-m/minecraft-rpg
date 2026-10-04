package soulsstats.stat;

import net.minecraft.server.level.ServerPlayer;
import soulsstats.data.Config;

/** Vigor → carga máxima: vigor_load (config.json) según los puntos sobre la base. La carga está en weight/Load. */
public final class VigorLoad {
	public static float max(ServerPlayer player) {
		return Config.get().vigorLoad().at(Stat.VIGOR.points(player));
	}
}
