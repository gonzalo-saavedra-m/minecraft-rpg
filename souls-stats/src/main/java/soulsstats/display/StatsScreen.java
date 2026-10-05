package soulsstats.display;

import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import soulsstats.progress.PlayerStatsPayload;
import soulsstats.progress.RaisePayload;
import soulsstats.stat.Stat;
import soulsstats.weight.Load;

/**
 * Pantalla de stats (tecla K): nivel, XP, puntos libres, cada stat con un botón + que reparte puntos (shift: varios)
 * y la carga. Se arma de nuevo cada vez que llegan datos del servidor (PlayerStatsPayload).
 */
public final class StatsScreen extends Screen {
	public static final int SHIFT_AMOUNT = 5;
	private static final int COLUMNS = 3, COLUMN_SPACING = 10, ROW_SPACING = 4;
	private HeaderAndFooterLayout layout;
	private final KeyMapping key;
	private PlayerStatsPayload shown;

	public StatsScreen(KeyMapping key) {
		super(Text.tr("screen.title", "Character"));
		this.key = key;
	}

	@Override
	protected void init() {
		layout = new HeaderAndFooterLayout(this);
		layout.addTitleHeader(title, font);
		Optional<PlayerStatsPayload> stats = PlayerStatsPayload.client();
		shown = stats.orElse(null);
		layout.addToContents(stats.map(this::grid).orElseGet(() -> {
			GridLayout empty = new GridLayout();
			empty.addChild(text(Text.tr("screen.no_server", "This server doesn't have Souls Stats").withStyle(ChatFormatting.GRAY)), 0, 0);
			return empty;
		}));
		layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).build());
		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}

	private GridLayout grid(PlayerStatsPayload stats) {
		GridLayout grid = new GridLayout().columnSpacing(COLUMN_SPACING).rowSpacing(ROW_SPACING);
		grid.defaultCellSetting().alignVerticallyMiddle();
		GridLayout.RowHelper rows = grid.createRowHelper(COLUMNS);
		rows.addChild(text(Text.tr("screen.level", "Level %s", Text.gold(stats.level()))));
		rows.addChild(text(Text.tr("screen.xp", "XP %s / %s", stats.xp(), stats.nextLevelXp()).withStyle(ChatFormatting.YELLOW)), COLUMNS - 1,
				rows.newCellSettings().alignHorizontallyRight());
		rows.addChild(text(stats.freePoints() > 0 ? Text.tr("screen.free_points", "%s free points", stats.freePoints()).withStyle(ChatFormatting.GREEN)
				: Text.tr("screen.no_free_points", "No free points").withStyle(ChatFormatting.GRAY)), COLUMNS);
		// Las stats en el orden del registro, solo las que el servidor conoce.
		Stat.REGISTRY.stream().filter(stats.stats()::containsKey).forEach(stat -> {
			rows.addChild(text(Text.name(stat)));
			rows.addChild(text(Text.gold(stats.stats().get(stat))), rows.newCellSettings().alignHorizontallyRight());
			Button raise = rows.addChild(Button.builder(Component.literal("+"), button -> raise(stat, stats.freePoints()))
					.size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT).tooltip(Tooltip.create(Text.tr("screen.raise", "Shift-click: +%s", SHIFT_AMOUNT))).build());
			raise.active = stats.freePoints() > 0;
		});
		rows.addChild(text(Text.tr("weight", "Weight %s / %s · %s", Text.gold(stats.weight()), Text.gold(stats.maxLoad()),
				Text.load(Load.of(stats.weight(), stats.maxLoad()))).withStyle(ChatFormatting.GRAY)), COLUMNS);
		return grid;
	}

	private StringWidget text(MutableComponent text) {
		return new StringWidget(text, font);
	}

	private void raise(Stat stat, int freePoints) {
		ClientPlayNetworking.send(new RaisePayload(stat.id(), minecraft.hasShiftDown() ? Math.min(SHIFT_AMOUNT, freePoints) : 1));
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
	}

	@Override
	public void tick() {
		if (PlayerStatsPayload.client().orElse(null) != shown) {
			rebuildWidgets();
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (key.matches(event)) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
