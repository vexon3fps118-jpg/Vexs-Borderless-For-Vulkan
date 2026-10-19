package com.vex.borderless;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VexsBorderless implements ClientModInitializer {
    public static final String MOD_ID = "vexs_borderless_for_vulkan";
    public static final Logger LOGGER = LoggerFactory.getLogger("Vexs Borderless For Vulkan");

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        BorderlessConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vexs_borderless.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F10,
                KeyBinding.Category.create(Identifier.of(MOD_ID, "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                long handle = client.getWindow().getHandle();
                boolean wasActive = BorderlessManager.isActive();
                boolean ok = BorderlessManager.toggle(handle);
                if (!wasActive && !ok) {
                    message(client, "vexs_borderless.blocked");
                    continue;
                }
                BorderlessConfig.startBorderless = BorderlessManager.isActive();
                BorderlessConfig.save();
                message(client, BorderlessManager.isActive() ? "vexs_borderless.on" : "vexs_borderless.off");
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (BorderlessConfig.startBorderless) {
                BorderlessManager.enable(client.getWindow().getHandle());
            }
        });

        LOGGER.info("Vexs Borderless For Vulkan loaded (press F10 to toggle)");
    }

    private static void message(MinecraftClient client, String key) {
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(key), true);
        }
    }
}
