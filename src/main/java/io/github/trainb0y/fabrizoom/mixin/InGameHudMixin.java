package io.github.trainb0y.fabrizoom.mixin;

import io.github.trainb0y.fabrizoom.ZoomLogic;
import io.github.trainb0y.fabrizoom.config.ConfigHandler;
import io.github.trainb0y.fabrizoom.config.ZoomTransition;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles rendering the zoom vignette and/or spyglass overlay
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Unique
	private static final Identifier ZOOM_OVERLAY = new Identifier("fabrizoom:textures/zoom_overlay.png");

	@Shadow
	private void renderSpyglassOverlay(DrawContext context, float scale) {}

	@Shadow
	private void renderOverlay(DrawContext context, Identifier texture, float opacity) {}

	@Inject(
			at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerInventory.getArmorStack(I)Lnet/minecraft/item/ItemStack;"),
			method = "render(Lnet/minecraft/client/gui/DrawContext;F)V"
	)
	public void injectZoomOverlay(DrawContext context, float tickDelta, CallbackInfo ci) {
		if (ZoomLogic.INSTANCE.getCurrentZoomFovMultiplier() >= 0.99) return;

		switch (ConfigHandler.getValues().getZoomOverlay()) {
			case NONE -> {}
			case VIGNETTE -> {
				float alpha;
				if (ConfigHandler.getValues().getTransition() != ZoomTransition.NONE) {
					// smooth and linear transition
					alpha = 1 - MathHelper.lerp(tickDelta, ZoomLogic.getLastZoomOverlayAlpha(), ZoomLogic.getZoomOverlayAlpha());
				} else {
					// no transition
					alpha = ZoomLogic.getZoomOverlayAlpha();
				}

				renderOverlay(context, ZOOM_OVERLAY, alpha);
			}
			case SPYGLASS -> {
				if (ZoomLogic.isZooming()) {
					float scale = (1f / (float) ZoomLogic.getZoomDivisor()) / ZoomLogic.INSTANCE.getCurrentZoomFovMultiplier();
					scale += 0.125f; // this is jank, but the vanilla spyglass lerps from 0.5 to 1.125
					scale = MathHelper.clamp(scale, 0.5f, 1.125f);
					this.renderSpyglassOverlay(context, scale);
				}
			}
		}
	}
}