package soulsstats.display;

import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import soulsstats.data.Config;
import soulsstats.data.ScalingGrade;
import soulsstats.item.ItemProperties;
import soulsstats.progress.PlayerStatsPayload;
import soulsstats.stat.Stat;
import soulsstats.weight.ItemWeight;

/**
 * Al final del tooltip (solo en el cliente): peso, stats que suma equipado ("+3 Vigor") y, en armas, una sección
 * "Requirements:" con una stat por línea ("Strength: 18", en rojo si el jugador no la cumple) y otra "Scaling:" con
 * su letra ("Strength: A"); con el tooltip avanzado (F3+H), la letra lleva su número: "Strength: A (1.2)".
 */
public final class ItemTooltip {
	/** " <label>: N", en rojo si el jugador no lo cumple; sin datos del servidor, sin marcar. */
	private static MutableComponent requirement(MutableComponent label, int required, Optional<Integer> have) {
		return Text.gray(" ").append(label.withStyle(ChatFormatting.GRAY)).append(Text.gray(": ")).append(Component.literal(String.valueOf(required))
				.withStyle(have.filter(value -> value < required).isPresent() ? ChatFormatting.RED : ChatFormatting.GOLD));
	}

	public static void register() {
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			ItemProperties item = ItemProperties.of(stack);
			float weight = ItemWeight.of(stack);
			if (weight > 0) {
				lines.add(Text.tr("tooltip.weight", "Weight %s", Text.gold(weight)).withStyle(ChatFormatting.GRAY));
			}
			for (Map.Entry<Stat, Integer> stat : item.stats().entrySet()) {
				int points = stat.getValue();
				lines.add(Component.literal((points > 0 ? "+" : "") + points + " ").append(Text.name(stat.getKey()))
						.withStyle(points > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
			}
			if (!item.requires().isEmpty() || item.usableAtLevel().isPresent() || item.equipableAtLevel().isPresent()) {
				lines.add(Text.tr("tooltip.requirements", "Requirements:").withStyle(ChatFormatting.GRAY));
				Optional<PlayerStatsPayload> player = PlayerStatsPayload.client();
				Optional<Integer> level = player.map(PlayerStatsPayload::level);
				item.usableAtLevel().ifPresent(required -> lines.add(requirement(Text.tr("tooltip.level", "Level"), required, level)));
				item.equipableAtLevel().ifPresent(required -> lines.add(requirement(Text.tr("tooltip.equip_level", "Level to equip"), required, level)));
				item.requires().forEach((stat, required) -> lines.add(requirement(Text.name(stat), required,
						player.map(stats -> stats.stats().getOrDefault(stat, stat.base)))));
			}
			Config config = Config.get();
			Map<Stat, Float> scaling = ItemProperties.scaling(stack);
			if (config != null && !scaling.isEmpty()) {
				lines.add(Text.tr("tooltip.scaling", "Scaling:").withStyle(ChatFormatting.GRAY));
				scaling.forEach((stat, value) -> lines.add(Text.gray(" ").append(Text.name(stat).withStyle(ChatFormatting.GRAY)).append(Text.gray(": ")).append(Component.literal(
						ScalingGrade.of(value, config.scalingGrades()).name()
								+ (flag.isAdvanced() ? " (" + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value) + ")" : ""))
						.withStyle(ChatFormatting.GOLD))));
			}
		});
	}
}
