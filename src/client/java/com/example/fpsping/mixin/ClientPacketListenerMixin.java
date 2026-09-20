package com.example.fpsping.mixin;

import com.example.fpsping.TpsTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Observes server time updates so the client can estimate server TPS:
 * the server sends an absolute gameTime once per second, so the rate of
 * change of gameTime over wall-clock time is the server's tick rate.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "handleSetTime", at = @At("HEAD"))
	private void fpsping$onSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
		TpsTracker.onTimeUpdate(packet.gameTime());
	}
}
