package net.meer.boot;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.lang.management.ManagementFactory;

/**
 * MEER own client bootstrap (S1 prototype, D-005): GLFW window + hardware probe (D-012).
 * Not a game yet — proves: own namespace, LWJGL3/GLFW path, GPU capability detection.
 */
public final class BootClient {

    public static void main(String[] args) throws Exception {
        long t0 = System.nanoTime();

        // --- hardware probe (D-012): no assumptions, everything detected ---
        Runtime rt = Runtime.getRuntime();
        int cores = rt.availableProcessors();
        long totalRam = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
        System.out.println("[probe] cpu_cores=" + cores + " total_ram_mb=" + (totalRam >> 20));

        if (!GLFW.glfwInit()) throw new IllegalStateException("glfwInit failed");
        GLFW.glfwSetErrorCallback((code, desc) -> System.err.println("[glfw] " + code + " " + MemoryUtil.memUTF8Safe(desc)));

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        // modern core context; fallback path (compat profile) comes in S3 per driver probe
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        long win = GLFW.glfwCreateWindow(1280, 720, "MEER client (own terminal)", MemoryUtil.NULL, MemoryUtil.NULL);
        if (win == MemoryUtil.NULL) throw new IllegalStateException("window creation failed");
        GLFW.glfwMakeContextCurrent(win);
        GLFW.glfwSwapInterval(1); // vsync default
        GLFW.glfwShowWindow(win);
        GL.createCapabilities();

        try (MemoryStack st = MemoryStack.stackPush()) {
            System.out.println("[gpu] vendor=" + GL11.glGetString(GL11.GL_VENDOR)
                + " renderer=" + GL11.glGetString(GL11.GL_RENDERER)
                + " version=" + GL11.glGetString(GL11.GL_VERSION));
        }
        GLFWVidMode vid = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (vid != null) System.out.println("[probe] primary_mode=" + vid.width() + "x" + vid.height());

        long t1 = System.nanoTime();
        System.out.println("[boot] window ready in " + ((t1 - t0) / 1_000_000) + " ms");

        double hue = 0.0;
        long last = System.nanoTime();
        int frames = 0;
        long fpsTimer = last;
        while (!GLFW.glfwWindowShouldClose(win)) {
            GLFW.glfwPollEvents();
            long now = System.nanoTime();
            double dt = (now - last) / 1e9;
            last = now;
            hue = (hue + dt * 0.1) % 1.0;
            GL11.glClearColor(0.5f * (float) Math.sin(hue * 6.283) + 0.5f, 0.2f, 0.35f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GLFW.glfwSwapBuffers(win);
            frames++;
            if (now - fpsTimer > 1_000_000_000L) {
                System.out.println("[loop] fps=" + frames);
                frames = 0;
                fpsTimer = now;
            }
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
                GLFW.glfwSetWindowShouldClose(win, true);
            }
        }
        GLFW.glfwDestroyWindow(win);
        GLFW.glfwTerminate();
        System.out.println("[boot] clean exit");
    }
}
