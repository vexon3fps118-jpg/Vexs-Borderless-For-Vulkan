package com.vex.borderless;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

/**
 * Borderless windowed fullscreen using ONLY GLFW window calls.
 * No OpenGL, no Vulkan, no mixins -> the renderer (vanilla GL or VulkanMod)
 * just sees a normal window resize and recreates its swapchain/framebuffer.
 * Must be called on the render/main thread.
 */
public final class BorderlessManager {
    private static boolean active = false;
    private static int savedX, savedY, savedW = 854, savedH = 480;

    private BorderlessManager() {}

    public static boolean isActive() {
        return active;
    }

    /** @return false if it could not be enabled (e.g. vanilla fullscreen is active). */
    public static boolean enable(long window) {
        if (active || window == 0L) return active;
        // Vanilla (exclusive) fullscreen owns the window -> don't fight it.
        if (GLFW.glfwGetWindowMonitor(window) != 0L) return false;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // If maximized, un-maximize first or the size/pos calls get ignored.
            if (GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE) {
                GLFW.glfwRestoreWindow(window);
            }

            IntBuffer x = stack.mallocInt(1), y = stack.mallocInt(1);
            IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1);
            GLFW.glfwGetWindowPos(window, x, y);
            GLFW.glfwGetWindowSize(window, w, h);
            savedX = x.get(0);
            savedY = y.get(0);
            savedW = w.get(0);
            savedH = h.get(0);

            long monitor = pickMonitor(stack, savedX, savedY, savedW, savedH);
            if (monitor == 0L) return false;

            GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);
            if (mode == null) return false;

            IntBuffer mx = stack.mallocInt(1), my = stack.mallocInt(1);
            GLFW.glfwGetMonitorPos(monitor, mx, my);

            GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowPos(window, mx.get(0), my.get(0));
            GLFW.glfwSetWindowSize(window, mode.width(), mode.height());
        }
        active = true;
        return true;
    }

    public static void disable(long window) {
        if (!active || window == 0L) return;
        // If the user went into vanilla fullscreen meanwhile, vanilla will restore the window itself.
        if (GLFW.glfwGetWindowMonitor(window) == 0L) {
            GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowSize(window, savedW, savedH);
            GLFW.glfwSetWindowPos(window, savedX, savedY);
        } else {
            GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
        }
        active = false;
    }

    public static boolean toggle(long window) {
        if (active) {
            disable(window);
            return false;
        }
        return enable(window);
    }

    /** Monitor containing the window's centre point; falls back to the primary monitor. */
    private static long pickMonitor(MemoryStack stack, int wx, int wy, int ww, int wh) {
        PointerBuffer monitors = GLFW.glfwGetMonitors();
        long primary = GLFW.glfwGetPrimaryMonitor();
        if (monitors == null) return primary;

        int cx = wx + ww / 2;
        int cy = wy + wh / 2;
        for (int i = 0; i < monitors.limit(); i++) {
            long m = monitors.get(i);
            GLFWVidMode mode = GLFW.glfwGetVideoMode(m);
            if (mode == null) continue;
            IntBuffer mx = stack.mallocInt(1), my = stack.mallocInt(1);
            GLFW.glfwGetMonitorPos(m, mx, my);
            if (cx >= mx.get(0) && cx < mx.get(0) + mode.width()
                    && cy >= my.get(0) && cy < my.get(0) + mode.height()) {
                return m;
            }
        }
        return primary;
    }
}
