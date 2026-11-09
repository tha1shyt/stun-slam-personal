package com.stunslam.controller;

import com.stunslam.config.StunSlamConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class StunSlamController {
    private static final StunSlamController INSTANCE = new StunSlamController();

    // Combo State Tracking
    private boolean isExecuting = false;
    private boolean isInternalSwap = false;
    private int lockCooldownTicks = 0;
    private int originalSlot = 0;

    // Queued follow-up state
    private int pendingStage = 0; // 0 = idle, 1 = wait for mace slam, 2 = wait for return
    private int stageTicks = 0;
    private Entity pendingTarget = null;
    private int pendingMaceSlot = -1;

    public static StunSlamController getInstance() {
        return INSTANCE;
    }

    /**
     * Checks if ToxMaceSwap and other external mods should be prevented from modifying hotbar slots.
     */
    public boolean isHotbarLocked() {
        return isExecuting || lockCooldownTicks > 0;
    }

    public boolean isInternalSwap() {
        return isInternalSwap;
    }

    /**
     * Determines whether the given target is actively guarding with a shield.
     */
    public boolean isTargetShielding(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;

        // Check if entity is currently blocking with a shield
        boolean blocking = living.isBlocking();
        if (!blocking && living.isUsingItem()) {
            ItemStack active = living.getActiveItem();
            blocking = active.isOf(Items.SHIELD);
        }

        if (!blocking) return false;

        StunSlamConfig config = StunSlamConfig.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();
        if (config.requireFacingShield && client.player != null) {
            // Check if attacker is inside the 180-degree blocking cone
            Vec3d targetPos = living.getPos();
            Vec3d attackerPos = client.player.getPos();
            Vec3d toAttacker = attackerPos.subtract(targetPos).normalize();
            Vec3d targetFacing = living.getRotationVec(1.0F).normalize();

            // 2D horizontal dot product
            double dot = (toAttacker.x * targetFacing.x) + (toAttacker.z * targetFacing.z);
            // Shield only blocks if attacker is in front (> 0.0 dot)
            return dot > 0.0;
        }

        return true;
    }

    /**
     * Checks if local player satisfies falling / velocity requirements.
     */
    public boolean canPerformSlam(ClientPlayerEntity player) {
        StunSlamConfig config = StunSlamConfig.getInstance();
        if (!config.requireFalling) return true;

        boolean isFalling = player.fallDistance > config.minFallDistance;
        boolean hasDownwardVelocity = player.getVelocity().y < -0.1 && !player.isOnGround();
        return isFalling || hasDownwardVelocity;
    }

    /**
     * Main entry point when player attacks. Returns true if Stun Slam handled the attack.
     */
    public boolean tryPerformStunSlam(MinecraftClient client, Entity target) {
        StunSlamConfig config = StunSlamConfig.getInstance();
        if (!config.enabled || client.player == null || client.interactionManager == null) return false;
        if (isExecuting) return true; // Already mid-combo

        if (!isTargetShielding(target)) return false;
        if (!canPerformSlam(client.player)) return false;

        // Locate weapons in hotbar
        int axeSlot = findBestAxeSlot(client.player);
        if (axeSlot == -1) {
            sendFeedback(client, Text.literal("No Axe found in hotbar for Stun Slam!").formatted(Formatting.RED));
            return false;
        }

        int maceSlot = findBestMaceSlot(client.player);
        if (maceSlot == -1) {
            sendFeedback(client, Text.literal("No Mace found in hotbar for Stun Slam!").formatted(Formatting.RED));
            return false;
        }

        // Begin Stun Slam sequence
        executeCombo(client, target, axeSlot, maceSlot);
        return true;
    }

    /**
     * Executes the stun slam sequence.
     */
    private void executeCombo(MinecraftClient client, Entity target, int axeSlot, int maceSlot) {
        StunSlamConfig config = StunSlamConfig.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) return;

        this.isExecuting = true;
        this.originalSlot = player.getInventory().selectedSlot;
        this.lockCooldownTicks = config.toxLockDurationTicks + Math.max(1, config.swapDelayTicks);

        // --- Step 1: Switch to Axe & Break Shield (Stun) ---
        switchToSlot(client, axeSlot);
        client.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);

        playActionSound(client, 1.2f);

        if (config.swapDelayTicks == 0) {
            // Instant Packet Burst (0 Ticks)
            // --- Step 2: Switch to Mace & Slam ---
            switchToSlot(client, maceSlot);
            client.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);

            playActionSound(client, 1.8f);
            sendFeedback(client, Text.literal("⚡ Stun Slam landed! (Instant)").formatted(Formatting.GREEN, Formatting.BOLD));

            // Post-combo recovery
            applyPostCombo(client, maceSlot);
            this.isExecuting = false;
        } else {
            // Queued Delay (1 or 2 ticks for Anti-Cheat tolerance)
            this.pendingStage = 1;
            this.stageTicks = config.swapDelayTicks;
            this.pendingTarget = target;
            this.pendingMaceSlot = maceSlot;
        }
    }

    /**
     * Called on each client tick to handle timed stages.
     */
    public void onTick(MinecraftClient client) {
        if (lockCooldownTicks > 0) {
            lockCooldownTicks--;
        }

        if (pendingStage == 1) {
            stageTicks--;
            if (stageTicks <= 0) {
                // Execute Mace Slam Stage
                if (client.player != null && client.interactionManager != null && pendingTarget != null && pendingTarget.isAlive()) {
                    switchToSlot(client, pendingMaceSlot);
                    client.interactionManager.attackEntity(client.player, pendingTarget);
                    client.player.swingHand(Hand.MAIN_HAND);

                    playActionSound(client, 1.8f);
                    sendFeedback(client, Text.literal("⚡ Stun Slam landed!").formatted(Formatting.GREEN, Formatting.BOLD));

                    applyPostCombo(client, pendingMaceSlot);
                }

                pendingStage = 0;
                pendingTarget = null;
                isExecuting = false;
            }
        }

        // Optional Auto Trigger Check (TriggerBot mode)
        StunSlamConfig config = StunSlamConfig.getInstance();
        if (config.enabled && config.triggerMode == StunSlamConfig.TriggerMode.TRIGGER_BOT && !isExecuting) {
            checkAutoTrigger(client);
        }
    }

    private void checkAutoTrigger(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            if (isTargetShielding(target) && canPerformSlam(client.player)) {
                tryPerformStunSlam(client, target);
            }
        }
    }

    private void switchToSlot(MinecraftClient client, int slot) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        try {
            this.isInternalSwap = true;
            client.player.getInventory().selectedSlot = slot;
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        } finally {
            this.isInternalSwap = false;
        }
    }

    private void applyPostCombo(MinecraftClient client, int currentMaceSlot) {
        StunSlamConfig config = StunSlamConfig.getInstance();
        if (client.player == null) return;

        switch (config.postComboAction) {
            case RETURN_PREVIOUS -> switchToSlot(client, originalSlot);
            case KEEP_MACE -> switchToSlot(client, currentMaceSlot);
            case SWITCH_TO_SWORD -> {
                int swordSlot = findSwordSlot(client.player);
                if (swordSlot != -1) {
                    switchToSlot(client, swordSlot);
                } else {
                    switchToSlot(client, originalSlot);
                }
            }
            case HANDOFF_TOX -> {
                // Instantly let ToxMaceSwap take over
                this.lockCooldownTicks = 0;
            }
        }
    }

    public int findBestAxeSlot(ClientPlayerEntity player) {
        int bestSlot = -1;
        int highestPriority = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof AxeItem) {
                int priority = getAxeTierPriority(stack);
                if (priority > highestPriority) {
                    highestPriority = priority;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    private int getAxeTierPriority(ItemStack stack) {
        if (stack.isOf(Items.NETHERITE_AXE)) return 6;
        if (stack.isOf(Items.DIAMOND_AXE)) return 5;
        if (stack.isOf(Items.IRON_AXE)) return 4;
        if (stack.isOf(Items.STONE_AXE)) return 3;
        if (stack.isOf(Items.GOLDEN_AXE)) return 2;
        if (stack.isOf(Items.WOODEN_AXE)) return 1;
        return 0;
    }

    public int findBestMaceSlot(ClientPlayerEntity player) {
        StunSlamConfig config = StunSlamConfig.getInstance();
        int fallbackSlot = -1;

        boolean isFalling = player.fallDistance > 1.2 || (player.getVelocity().y < -0.15 && !player.isOnGround());

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isMace(stack)) {
                if (fallbackSlot == -1) fallbackSlot = i;

                if (config.smartMaceSelection) {
                    ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
                    if (enchants != null) {
                        // Check if mace matches combat state (Density when falling, Breach when grounded)
                        boolean hasDensity = enchants.getEnchantments().stream()
                                .anyMatch(e -> e.matchesKey(Enchantments.DENSITY));
                        boolean hasBreach = enchants.getEnchantments().stream()
                                .anyMatch(e -> e.matchesKey(Enchantments.BREACH));

                        if (isFalling && hasDensity) return i;
                        if (!isFalling && hasBreach) return i;
                    }
                }
            }
        }
        return fallbackSlot;
    }

    private boolean isMace(ItemStack stack) {
        return stack.isOf(Items.MACE) || stack.getItem() instanceof MaceItem ||
                stack.getItem().getTranslationKey().toLowerCase().contains("mace");
    }

    private int findSwordSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() instanceof SwordItem) {
                return i;
            }
        }
        return -1;
    }

    private void playActionSound(MinecraftClient client, float pitch) {
        StunSlamConfig config = StunSlamConfig.getInstance();
        if (config.soundFeedback && client.player != null && client.world != null) {
            client.world.playSound(client.player, client.player.getX(), client.player.getY(), client.player.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.7F, pitch);
        }
    }

    private void sendFeedback(MinecraftClient client, Text message) {
        StunSlamConfig config = StunSlamConfig.getInstance();
        if (config.hudNotification && client.player != null) {
            client.player.sendMessage(message, true); // true = action bar overlay
        }
    }
}
