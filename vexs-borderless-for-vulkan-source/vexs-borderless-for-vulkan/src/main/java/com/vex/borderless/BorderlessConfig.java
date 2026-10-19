package com.vex.borderless;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Tiny properties-file config: .minecraft/config/vexs_borderless_for_vulkan.properties */
public final class BorderlessConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("vexs_borderless_for_vulkan.properties");

    /** Re-enable borderless automatically on game start if it was on last time. */
    public static boolean startBorderless = false;

    private BorderlessConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try (Reader r = Files.newBufferedReader(FILE)) {
            Properties p = new Properties();
            p.load(r);
            startBorderless = Boolean.parseBoolean(p.getProperty("startBorderless", "false"));
        } catch (IOException e) {
            VexsBorderless.LOGGER.warn("Could not read config", e);
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("startBorderless", Boolean.toString(startBorderless));
        try (Writer w = Files.newBufferedWriter(FILE)) {
            p.store(w, "Vexs Borderless For Vulkan");
        } catch (IOException e) {
            VexsBorderless.LOGGER.warn("Could not write config", e);
        }
    }
}
