package com.stunslam;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.stunslam.config.StunSlamConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class StunSlamMod implements ClientModInitializer {
    public static final String MOD_ID = "stunslam";
    private static KeyBinding toggleKeyBinding;

    @Override
    public void onInitializeClient() {
        System.out.println("[StunSlam] Initializing Stun Slam (Optimal & ToxMaceSwap Compatible)...");

        // Load configuration
        StunSlamConfig.getInstance();

        // Register Toggle Keybinding
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stunslam.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.stunslam"
        ));

        // Register Client Tick Event for Keypresses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKeyBinding.wasPressed()) {
                StunSlamConfig config = StunSlamConfig.getInstance();
                config.enabled = !config.enabled;
                config.save();

                if (client.player != null) {
                    Formatting format = config.enabled ? Formatting.GREEN : Formatting.RED;
                    String status = config.enabled ? "ENABLED" : "DISABLED";
                    client.player.sendMessage(Text.literal("⚡ Stun Slam: " + status).formatted(format, Formatting.BOLD), true);
                }
            }
        });

        // Register in-game chat commands (/stunslam or /ss)
        registerCommands();

        System.out.println("[StunSlam] Stun Slam successfully initialized!");
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("stunslam")
                    .then(ClientCommandManager.literal("toggle")
                            .executes(context -> {
                                StunSlamConfig config = StunSlamConfig.getInstance();
                                config.enabled = !config.enabled;
                                config.save();
                                Formatting format = config.enabled ? Formatting.GREEN : Formatting.RED;
                                String status = config.enabled ? "ENABLED" : "DISABLED";
                                context.getSource().sendFeedback(Text.literal("⚡ Stun Slam is now: " + status).formatted(format));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("delay")
                            .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, 5))
                                    .executes(context -> {
                                        int ticks = IntegerArgumentType.getInteger(context, "ticks");
                                        StunSlamConfig config = StunSlamConfig.getInstance();
                                        config.swapDelayTicks = ticks;
                                        config.save();
                                        context.getSource().sendFeedback(Text.literal("⚡ Stun Slam swap delay set to: " + ticks + " ticks").formatted(Formatting.GOLD));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("reload")
                            .executes(context -> {
                                StunSlamConfig.load();
                                context.getSource().sendFeedback(Text.literal("⚡ Stun Slam configuration reloaded!").formatted(Formatting.AQUA));
                                return 1;
                            }))
                    .executes(context -> {
                        StunSlamConfig config = StunSlamConfig.getInstance();
                        context.getSource().sendFeedback(Text.literal("=== ⚡ Stun Slam Mod ===").formatted(Formatting.YELLOW, Formatting.BOLD));
                        context.getSource().sendFeedback(Text.literal("Status: " + (config.enabled ? "§aENABLED" : "§cDISABLED")));
                        context.getSource().sendFeedback(Text.literal("Trigger Mode: §e" + config.triggerMode.getDisplayName()));
                        context.getSource().sendFeedback(Text.literal("Swap Delay: §e" + config.swapDelayTicks + " ticks"));
                        context.getSource().sendFeedback(Text.literal("ToxMaceSwap Lock: §e" + (config.toxMaceSwapCompat ? "ON" : "OFF")));
                        context.getSource().sendFeedback(Text.literal("Press 'V' to toggle or configure via Mod Menu.").formatted(Formatting.GRAY));
                        return 1;
                    })
            );

            // Shorthand alias: /ss
            dispatcher.register(ClientCommandManager.literal("ss")
                    .executes(context -> {
                        StunSlamConfig config = StunSlamConfig.getInstance();
                        config.enabled = !config.enabled;
                        config.save();
                        Formatting format = config.enabled ? Formatting.GREEN : Formatting.RED;
                        String status = config.enabled ? "ENABLED" : "DISABLED";
                        context.getSource().sendFeedback(Text.literal("⚡ Stun Slam: " + status).formatted(format));
                        return 1;
                    })
            );
        });
    }
}
