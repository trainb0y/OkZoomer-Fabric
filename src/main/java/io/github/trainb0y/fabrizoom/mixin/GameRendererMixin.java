package io.github.trainb0y.fabrizoom.mixin;

import io.github.trainb0y.fabrizoom.Zoom;
import io.github.trainb0y.fabrizoom.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//This mixin is responsible for managing the fov-changing part of the zoom.
@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Final
	@Shadow
	private MinecraftClient client;

	//Handle transitioned zoom FOV multiplier and zoom overlay alphas each tick.
	@Inject(
			at = @At("HEAD"),
			method = "tick()V"
	)
	private void zoomTick(CallbackInfo info) {
		//If zoom transitions are enabled, update the zoom FOV multiplier.
		if (Config.getZoomTransition() && Zoom.getZooming()) {
			Zoom.updateZoomFovMultiplier();
		}
	}

	//Handles zooming of both modes (Transitionless and with Smooth Transitions).
	@Inject(
			at = @At("RETURN"),
			method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D",
			cancellable = true
	)
	private double getZoomedFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> info) {
		double fov = info.getReturnValue();

		if (Config.getZoomTransition()) {
			//Handle the zoom with smooth transitions enabled.
			if (Zoom.getCurrentZoomFovMultiplier() != 1.0F) {
				fov *= MathHelper.lerp(tickDelta, Zoom.getLastZoomFovMultiplier(), Zoom.getCurrentZoomFovMultiplier());
				info.setReturnValue(fov);
			}
		} else if (Zoom.getZooming()){
			//Handle the zoom without smooth transitions.
			double zoomedFov = fov / Zoom.getZoomDivisor();
			info.setReturnValue(zoomedFov);
		}

		/* //todo: fix this
		//Regardless of the mode, if the zoom is over, update the terrain in order to stop terrain glitches.
		if (Zoom.getLastZoomState() && changingFov) {
			this.client.worldRenderer.scheduleTerrainUpdate();
		}
		 */

		return fov;
	}
}