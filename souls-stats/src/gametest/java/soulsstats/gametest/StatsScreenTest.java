package soulsstats.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.tutorial.TutorialSteps;
import soulsstats.display.StatsScreen;
import soulsstats.progress.PlayerStatsPayload;

/**
 * Guarda capturas en build/run/clientGameTest/screenshots/: el toast de subida de nivel y la pantalla de stats (tecla K)
 * con puntos libres y equipo de hierro, y tras subir vigor con shift-click (+5). Corre con ./gradlew :souls-stats:runClientGameTest.
 * Solo usa lo que la API de tests tiene desde 26.1 (ADR-0010 de la raíz).
 */
public class StatsScreenTest implements FabricClientGameTest {
	private static final int LEVELS = 9, FREE_BEFORE = LEVELS - 1, FREE_AFTER = FREE_BEFORE - StatsScreen.SHIFT_AMOUNT, TOAST_TICKS = 20;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			// Sin los toasts del tutorial, que tapan el de subida de nivel.
			context.runOnClient(client -> client.getTutorial().setStep(TutorialSteps.NONE));
			world.getServer().runCommand("soulsstats levels @a " + LEVELS);
			waitForStats(context, stats -> stats.level() == LEVELS + 1);
			context.waitTicks(TOAST_TICKS);
			context.takeScreenshot("level-up-toast");
			world.getServer().runCommand("execute as @a run soulsstats raise strength");
			world.getServer().runCommand("item replace entity @a armor.chest with minecraft:iron_chestplate");
			world.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:iron_sword");
			waitForStats(context, stats -> stats.freePoints() == FREE_BEFORE && stats.weight() > 0);
			context.getInput().pressKey(InputConstants.KEY_K);
			context.waitForScreen(StatsScreen.class);
			context.takeScreenshot("stats-screen");
			context.getInput().holdShift();
			context.clickScreenButton("+");
			context.getInput().releaseShift();
			waitForStats(context, stats -> stats.freePoints() == FREE_AFTER);
			context.waitTick();
			context.takeScreenshot("stats-screen-raised");
		}
	}

	private static void waitForStats(ClientGameTestContext context, Predicate<PlayerStatsPayload> condition) {
		context.waitFor(client -> PlayerStatsPayload.client().filter(condition).isPresent());
	}
}
