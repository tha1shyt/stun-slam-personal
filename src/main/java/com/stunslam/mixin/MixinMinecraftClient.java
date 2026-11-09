package com.stunslam.mixin;

import com.stunslam.config.StunSlamConfig;
import com.stunslam.controller.StunSlamController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow public HitResult crosshairTarget;
    @Shadow public ClientPlayerEntity player;
    @Shadow public ClientPlayerInteractionManager interactionManager;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        StunSlamConfig config = StunSlamConfig.getInstance();

        if (!config.enabled || player == null || interactionManager == null) return;

        // Find candidate target
        Entity target = null;
        if (crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.ENTITY) {
            target = ((EntityHitResult) crosshairTarget).getEntity();
        } else {
            // Extended reach raycast check up to config.maxReach
            target = findTargetInReach(client, config.maxReach);
        }

        if (target != null && StunSlamController.getInstance().isTargetShielding(target)) {
            // Target is holding shield! Execute 100% accurate Stun Slam
            boolean handled = StunSlamController.getInstance().tryPerformStunSlam(client, target);
            if (handled) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    private Entity findTargetInReach(MinecraftClient client, double reachDistance) {
        if (client.player == null || client.world == null) return null;

        Vec3d cameraPos = client.player.getCameraPosVec(1.0F);
        Vec3d rotation = client.player.getRotationVec(1.0F);
        Vec3d reachVec = cameraPos.add(rotation.x * reachDistance, rotation.y * reachDistance, rotation.z * reachDistance);

        Box box = client.player.getBoundingBox().stretch(rotation.multiply(reachDistance)).expand(1.0D, 1.0D, 1.0D);
        EntityHitResult hitResult = ProjectileUtil.raycast(
                client.player,
                cameraPos,
                reachVec,
                box,
                entity -> !entity.isSpectator() && entity.canHit(),
                reachDistance * reachDistance
        );

        return hitResult != null ? hitResult.getEntity() : null;
    }
}
