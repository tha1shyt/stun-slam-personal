package com.stunslam.gui;

import com.stunslam.config.StunSlamConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class StunSlamConfigScreen {

    public static Screen createScreen(Screen parent) {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            return null;
        }

        StunSlamConfig config = StunSlamConfig.getInstance();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("⚡ Stun Slam Configuration"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // --- General Category ---
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Stun Slam"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Master switch to enable or disable the Stun Slam mod."))
                .setSaveConsumer(val -> config.enabled = val)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(Text.literal("Trigger Mode"), StunSlamConfig.TriggerMode.class, config.triggerMode)
                .setDefaultValue(StunSlamConfig.TriggerMode.ON_SHIELD_CLICK)
                .setTooltip(Text.literal("ON_SHIELD_CLICK: Auto executes stun slam whenever clicking an opponent who is blocking.\nTRIGGER_BOT: Automatically triggers upon aiming at an opponent blocking.\nKEYBIND_ONLY: Only triggers when holding the designated keybind."))
                .setSaveConsumer(val -> config.triggerMode = val)
                .build());

        // --- Combat & Timing Category ---
        ConfigCategory combat = builder.getOrCreateCategory(Text.literal("Combat & Timing"));

        combat.addEntry(entryBuilder.startIntSlider(Text.literal("Swap Delay Ticks"), config.swapDelayTicks, 0, 5)
                .setDefaultValue(1)
                .setTooltip(Text.literal("0 = Instant (packet burst in same tick).\n1 = Safe (recommended for anti-cheats like GrimAC/Vulcan).\n2 = High latency servers."))
                .setSaveConsumer(val -> config.swapDelayTicks = val)
                .build());

        combat.addEntry(entryBuilder.startDoubleField(Text.literal("Reach Distance (Blocks)"), config.maxReach)
                .setDefaultValue(3.8)
                .setMin(2.5)
                .setMax(6.0)
                .setTooltip(Text.literal("Maximum distance from which a shielding target can be stun-slammed."))
                .setSaveConsumer(val -> config.maxReach = val)
                .build());

        combat.addEntry(entryBuilder.startBooleanToggle(Text.literal("Require Target Facing Shield"), config.requireFacingShield)
                .setDefaultValue(true)
                .setTooltip(Text.literal("If enabled, only stuns if the target's shield is facing you (within the 180° blocking arc). If target is turned away, your mace deals full unblocked damage without wasting an axe hit."))
                .setSaveConsumer(val -> config.requireFacingShield = val)
                .build());

        combat.addEntry(entryBuilder.startBooleanToggle(Text.literal("Require Falling Momentum"), config.requireFalling)
                .setDefaultValue(false)
                .setTooltip(Text.literal("If enabled, Stun Slam only triggers when you have falling momentum/smash bonus."))
                .setSaveConsumer(val -> config.requireFalling = val)
                .build());

        // --- ToxMaceSwap Compatibility Category ---
        ConfigCategory compat = builder.getOrCreateCategory(Text.literal("ToxMaceSwap Compatibility"));

        compat.addEntry(entryBuilder.startBooleanToggle(Text.literal("ToxMaceSwap Compatibility Lock"), config.toxMaceSwapCompat)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Prevents ToxMaceSwap and external mods from overriding your hotbar slot while Stun Slam is mid-combo."))
                .setSaveConsumer(val -> config.toxMaceSwapCompat = val)
                .build());

        compat.addEntry(entryBuilder.startIntSlider(Text.literal("Hotbar Lock Duration (Ticks)"), config.toxLockDurationTicks, 1, 5)
                .setDefaultValue(2)
                .setTooltip(Text.literal("How many ticks other mods are prevented from switching hotbar slots after a Stun Slam."))
                .setSaveConsumer(val -> config.toxLockDurationTicks = val)
                .build());

        compat.addEntry(entryBuilder.startEnumSelector(Text.literal("Post-Combo Action"), StunSlamConfig.PostComboAction.class, config.postComboAction)
                .setDefaultValue(StunSlamConfig.PostComboAction.RETURN_PREVIOUS)
                .setTooltip(Text.literal("What slot to switch to after delivering the Mace Slam."))
                .setSaveConsumer(val -> config.postComboAction = val)
                .build());

        compat.addEntry(entryBuilder.startBooleanToggle(Text.literal("Smart Mace Selection"), config.smartMaceSelection)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Equips Density Mace when falling, and Breach Mace when on ground or against armored targets."))
                .setSaveConsumer(val -> config.smartMaceSelection = val)
                .build());

        // --- Feedback Category ---
        ConfigCategory feedback = builder.getOrCreateCategory(Text.literal("Feedback & HUD"));

        feedback.addEntry(entryBuilder.startBooleanToggle(Text.literal("Sound Feedback"), config.soundFeedback)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Plays a sound effect when a Stun Slam successfully lands."))
                .setSaveConsumer(val -> config.soundFeedback = val)
                .build());

        feedback.addEntry(entryBuilder.startBooleanToggle(Text.literal("Action Bar HUD Notification"), config.hudNotification)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Displays combat status text in your Action Bar."))
                .setSaveConsumer(val -> config.hudNotification = val)
                .build());

        builder.setSavingRunnable(config::save);
        return builder.build();
    }
}
