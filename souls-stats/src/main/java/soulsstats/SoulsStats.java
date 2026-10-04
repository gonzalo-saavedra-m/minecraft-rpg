package soulsstats;

import net.fabricmc.api.ModInitializer;
import org.slf4j.LoggerFactory;

public class SoulsStats implements ModInitializer {
	@Override
	public void onInitialize() {
		LoggerFactory.getLogger("soulsstats").info("Hello world");
	}
}
