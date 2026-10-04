package soulsstats.weight;

import java.util.Arrays;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import soulsstats.event.PlayerRefresh;
import soulsstats.stat.DexteritySpeed;
import soulsstats.stat.VigorLoad;

/**
 * Carga = peso equipado / carga máxima (stat/VigorLoad). Cada nivel llega hasta una fracción de la carga máxima
 * y multiplica la velocidad y suma al salto, levemente.
 */
// ponytail: umbrales (los de Dark Souls 3), velocidades y saltos provisionales; calibrar jugando.
public enum Load {
	// Salto: 0.42 (vanilla) sube ~1,25 bloques; 0.44 ~1,37, sin llegar a una valla (1,5); 0.39 ~1,1; 0.35 ~0,9.
	LIGHT(0.3f, 1.15f, 0.02), NORMAL(0.7f, 1, 0), HEAVY(1, 0.8f, -0.03), OVERLOADED(Float.POSITIVE_INFINITY, 0.6f, -0.07);

	private static final Identifier JUMP = Identifier.fromNamespaceAndPath("soulsstats", "load");
	private static final float VANILLA_WALKING_SPEED = 0.1f;
	private final float upTo, speed;
	private final double jump;

	Load(float upTo, float speed, double jump) {
		this.upTo = upTo;
		this.speed = speed;
		this.jump = jump;
	}

	public static void register() {
		PlayerRefresh.register(Load::apply);
	}

	public static Load of(float weight, float max) {
		return Arrays.stream(values()).filter(load -> weight / max <= load.upTo).findFirst().orElseThrow();
	}

	/** En ambos lados: el modificador de salto de la carga llega al cliente, y su valor es único por nivel. */
	public static boolean overloaded(Player player) {
		AttributeModifier modifier = player.getAttribute(Attributes.JUMP_STRENGTH).getModifier(JUMP);
		return modifier != null && modifier.amount() == OVERLOADED.jump;
	}

	private static void apply(ServerPlayer player) {
		Load load = of(ItemWeight.carried(player), VigorLoad.max(player));
		// La velocidad va en walkingSpeed y en la base del atributo, no en un modificador: el cliente hace el zoom
		// del campo de visión con atributo / walkingSpeed, así que moviendo ambos no hay zoom de poción, ni en
		// clientes sin el mod. Al cargar el jugador, Minecraft copia walkingSpeed a la base.
		float speed = VANILLA_WALKING_SPEED * load.speed * DexteritySpeed.movement(player);
		if (player.getAbilities().getWalkingSpeed() != speed) {
			player.getAbilities().setWalkingSpeed(speed);
			player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
			player.onUpdateAbilities();
		}
		player.getAttribute(Attributes.JUMP_STRENGTH).addOrReplacePermanentModifier(
				new AttributeModifier(JUMP, load.jump, AttributeModifier.Operation.ADD_VALUE));
	}
}
