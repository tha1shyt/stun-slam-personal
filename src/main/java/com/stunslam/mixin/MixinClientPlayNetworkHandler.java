package com.stunslam.mixin;

import com.stunslam.config.StunSlamConfig;
import com.stunslam.controller.StunSlamController;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof UpdateSelectedSlotC2SPacket) {
            StunSlamConfig config = StunSlamConfig.getInstance();
            StunSlamController controller = StunSlamController.getInstance();

            // If ToxMaceSwap compatibility is enabled and hotbar is locked during combo:
            if (config.toxMaceSwapCompat && controller.isHotbarLocked()) {
                // If this packet was triggered externally (e.g. by ToxMaceSwap or accidental scroll), block it!
                if (!controller.isInternalSwap()) {
                    ci.cancel();
                }
            }
        }
    }
}
