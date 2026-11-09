package com.stunslam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StunSlamConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "stunslam.json");

    private static StunSlamConfig INSTANCE;

    public enum TriggerMode {
        ON_SHIELD_CLICK("Click on Shield (Recommended)"),
        TRIGGER_BOT("Auto Trigger (Crosshair)"),
        KEYBIND_ONLY("Keybind Hold Only");

        private final String displayName;
        TriggerMode(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public enum PostComboAction {
        RETURN_PREVIOUS("Return to Previous Slot"),
        KEEP_MACE("Keep Mace Equipped"),
        SWITCH_TO_SWORD("Switch to Sword"),
        HANDOFF_TOX("Hand Off to ToxMaceSwap");

        private final String displayName;
        PostComboAction(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // --- Configuration Values ---
    public boolean enabled = true;
    public int toggleKey = GLFW.GLFW_KEY_V;
    public TriggerMode triggerMode = TriggerMode.ON_SHIELD_CLICK;

    // Combat Timing & Optimization
    public int swapDelayTicks = 1; // 0 = instant (same tick), 1 = anti-cheat safe (50ms), 2 = conservative
    public double maxReach = 3.8; // Reach distance in blocks
    public boolean requireFacingShield = true; // Shield angle check (180 degree cone)
    public boolean requireFalling = false; // Require downward momentum
    public double minFallDistance = 0.0; // Fall distance threshold

    // ToxMaceSwap Compatibility
    public boolean toxMaceSwapCompat = true; // Prevent ToxMaceSwap from overwriting hotbar during axe hit
    public int toxLockDurationTicks = 2; // Ticks to lock slot switching during combo

    // Hotbar Management
    public PostComboAction postComboAction = PostComboAction.RETURN_PREVIOUS;
    public boolean smartMaceSelection = true; // Prioritize Density if falling, Breach if grounded

    // Audio & Visual Feedback
    public boolean soundFeedback = true;
    public boolean hudNotification = true;

    public static StunSlamConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static StunSlamConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                StunSlamConfig config = GSON.fromJson(reader, StunSlamConfig.class);
                if (config != null) return config;
            } catch (IOException e) {
                System.err.println("[StunSlam] Failed to load config file: " + e.getMessage());
            }
        }
        StunSlamConfig defaultConfig = new StunSlamConfig();
        defaultConfig.save();
        return defaultConfig;
    }

    public void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[StunSlam] Failed to save config file: " + e.getMessage());
        }
    }
}
