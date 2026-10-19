# Vexs Borderless For Vulkan
Fabric client mod for Minecraft Java 1.21.11. Borderless fullscreen via pure GLFW,
so it works with VulkanMod (and vanilla OpenGL).

- Toggle: **F10** (rebind in Controls)
- Remembers state: config/vexs_borderless_for_vulkan.properties (startBorderless=true/false)
- Keep VulkanMod's own fullscreen OFF and leave vanilla F11 fullscreen off while borderless.

Build: `gradle wrapper` (once), then `./gradlew build` -> build/libs/vexs-borderless-for-vulkan-1.0.0.jar
Needs JDK 21.
