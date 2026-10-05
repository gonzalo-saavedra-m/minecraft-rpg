package soulsstats.display;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import soulsstats.progress.PlayerStatsPayload;

/** Aviso arriba a la derecha al subir de nivel, en vez del chat: el nivel nuevo y los puntos libres (con la tecla K). */
public final class LevelUpToast implements Toast {
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("toast/advancement"),
			ICON = Identifier.fromNamespaceAndPath("soulsstats", "level_up");
	private static final long DISPLAY_MILLIS = 5000;
	private static final int ICON_X = 8, ICON_Y = 8, ICON_SIZE = 16, TEXT_X = 30, TITLE_Y = 7, MESSAGE_Y = 18;
	private static ToastManager manager;
	private final Component title, message;
	private Visibility visibility = Visibility.SHOW;

	private LevelUpToast(Component title, Component message) {
		this.title = title;
		this.message = message;
	}

	/** Lo llama ToastManagerMixin al crearse: obtener el ToastManager cambió de clase en 26.2 (ADR-0010 de la raíz). */
	public static void use(ToastManager created) {
		manager = created;
	}

	public static void show(PlayerStatsPayload stats, KeyMapping key) {
		manager.addToast(new LevelUpToast(Text.tr("toast.level_up", "Level %s!", stats.level()).withStyle(ChatFormatting.GOLD),
				stats.freePoints() > 0 ? Text.tr("toast.free_points", "%s free points · %s", stats.freePoints(), key.getTranslatedKeyMessage())
						: Component.empty()));
	}

	@Override
	public Visibility getWantedVisibility() {
		return visibility;
	}

	@Override
	public void update(ToastManager toasts, long time) {
		visibility = time >= DISPLAY_MILLIS * toasts.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long time) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, width(), height());
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON, ICON_X, ICON_Y, ICON_SIZE, ICON_SIZE);
		graphics.text(font, title, TEXT_X, TITLE_Y, CommonColors.WHITE, false);
		graphics.text(font, message, TEXT_X, MESSAGE_Y, CommonColors.WHITE, false);
	}
}
