package net.meer.client;

import net.meer.proto.Proto;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

/**
 * MEER own client terminal (M1 core, D-005): connects to our server, receives world
 * snapshots, renders via own GL 3.3-core renderer. Zero simulation — display only.
 */
public final class ClientMain {
    static int sx, sy, sz;
    static byte[] world;
    static int vao, vbo, vertexCount, mvpUniform, program;
    static float camX = 64, camY = 40, camZ = 64, yaw, pitch;
    static long window;
    static double lastX, lastY;

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 25577;

        // --- network: pull authoritative snapshot ---
        try (Socket sock = new Socket(host, port);
             DataInputStream in = new DataInputStream(sock.getInputStream());
             DataOutputStream out = new DataOutputStream(sock.getOutputStream())) {
            Proto.writeFrame(out, Proto.HELLO_C, "MEER-C1".getBytes(StandardCharsets.UTF_8));
            byte[] w = Proto.readFrame(in);
            if (w[0] != Proto.WELCOME) throw new IllegalStateException("no welcome, type=" + w[0]);
            try (var s = new java.io.DataInputStream(new java.io.ByteArrayInputStream(w))) {
                s.readFully(new byte[8]); // type byte + "MEER-S1"
                sx = s.readInt(); sy = s.readInt(); sz = s.readInt();
            }
            byte[] chunk = Proto.readFrame(in);
            if (chunk[0] != Proto.CHUNK) throw new IllegalStateException("no chunk, type=" + chunk[0]);
            try (var s = new java.io.DataInputStream(new java.io.ByteArrayInputStream(chunk))) {
                s.readFully(new byte[1 + 24]); // type + 6 ints (dims + origin)
                world = new byte[sx * sy * sz];
                s.readFully(world);
            }
            System.out.println("[net] snapshot " + sx + "x" + sy + "x" + sz + " from " + host);
        }

        // --- GL ---
        if (!GLFW.glfwInit()) throw new IllegalStateException("glfw");
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        window = GLFW.glfwCreateWindow(1280, 720, "MEER client", MemoryUtil.NULL, MemoryUtil.NULL);
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        GLFW.glfwSetCursorPosCallback(window, (w, x, yy) -> {
            yaw += (x - lastX) * 0.15;
            pitch += (yy - lastY) * 0.15;
            pitch = Math.max(-89, Math.min(89, pitch));
            lastX = x; lastY = yy;
        });
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();

        buildMesh();
        buildShaders();
        System.out.println("[gl] vendor=" + GL11.glGetString(GL11.GL_VENDOR) + " version=" + GL11.glGetString(GL11.GL_VERSION));
        System.out.println("[render] vertices=" + vertexCount);

        long t0 = System.nanoTime();
        int frames = 0;
        long fpsT = t0, last = t0;
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();
            float dt = (System.nanoTime() - last) / 1e9f;
            last = System.nanoTime();
            move(dt);
            GL11.glClearColor(0.55f, 0.75f, 0.95f, 1f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            FloatBuffer m = matrix(70f, 1280f / 720f);
            GL20.glUseProgram(program);
            GL20.glUniformMatrix4fv(mvpUniform, false, m);
            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
            GL30.glBindVertexArray(0);
            GL20.glUseProgram(0);
            GLFW.glfwSwapBuffers(window);

            frames++;
            if (System.nanoTime() - fpsT > 1_000_000_000L) {
                System.out.println("[loop] fps=" + frames + " cam=" + (int) camX + "," + (int) camY + "," + (int) camZ);
                frames = 0; fpsT = System.nanoTime();
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) GLFW.glfwSetWindowShouldClose(window, true);
        }
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    static void move(float dt) {
        float sp = 24 * dt;
        double r = Math.toRadians(yaw);
        float fx = (float) -Math.sin(r), fz = (float) -Math.cos(r);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) { camX += fx * sp; camZ += fz * sp; }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) { camX -= fx * sp; camZ -= fz * sp; }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) { camX -= fz * sp; camZ += fx * sp; }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) { camX += fz * sp; camZ -= fx * sp; }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) camY += sp;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) camY -= sp;
    }

    static void buildMesh() {
        FloatBuffer fb = MemoryUtil.memAllocFloat(sx * sy * sz * 6 * 18);
        float[] faceColors = new float[4];
        for (int y = 0; y < sy; y++)
            for (int z = 0; z < sz; z++)
                for (int x = 0; x < sx; x++) {
                    byte id = world[idx(x, y, z)];
                    if (id == 0) continue;
                    float r, g, b;
                    if (id == 1) { r = 0.3f; g = 0.7f; b = 0.2f; }       // grass
                    else if (id == 2) { r = 0.45f; g = 0.3f; b = 0.15f; } // dirt
                    else { r = 0.5f; g = 0.5f; b = 0.55f; }               // stone
                    emitSolid(fb, x, y, z, r, g, b);
                }
        fb.flip();
        vertexCount = fb.remaining() / 6;
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        MemoryUtil.memFree(fb);
    }

    static void emitSolid(FloatBuffer fb, int x, int y, int z, float r, float g, float b) {
        if (solid(x, y + 1, z)) emitFace(fb, x, y + 1, z, x, y + 1, z + 1, x + 1, y + 1, z + 1, x + 1, y + 1, z, r * 1.15f, g * 1.15f, b * 1.15f);
        if (solid(x, y - 1, z)) emitFace(fb, x, y, z, x + 1, y, z, x + 1, y, z + 1, x, y, z + 1, r * 0.55f, g * 0.55f, b * 0.55f);
        if (solid(x - 1, y, z)) emitFace(fb, x, y, z, x, y, z + 1, x, y + 1, z + 1, x, y + 1, z, r * 0.85f, g * 0.85f, b * 0.85f);
        if (solid(x + 1, y, z)) emitFace(fb, x + 1, y, z, x + 1, y + 1, z, x + 1, y + 1, z + 1, x + 1, y, z + 1, r * 0.85f, g * 0.85f, b * 0.85f);
        if (solid(x, y, z - 1)) emitFace(fb, x, y, z, x, y + 1, z, x + 1, y + 1, z, x + 1, y, z, r * 0.7f, g * 0.7f, b * 0.7f);
        if (solid(x, y, z + 1)) emitFace(fb, x, y, z + 1, x + 1, y, z + 1, x + 1, y + 1, z + 1, x, y + 1, z + 1, r * 0.7f, g * 0.7f, b * 0.7f);
    }

    static void emitFace(FloatBuffer fb, float x1, float y1, float z1, float x2, float y2, float z2,
                         float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b) {
        put(fb, x1, y1, z1, r, g, b); put(fb, x2, y2, z2, r, g, b); put(fb, x3, y3, z3, r, g, b);
        put(fb, x1, y1, z1, r, g, b); put(fb, x3, y3, z3, r, g, b); put(fb, x4, y4, z4, r, g, b);
    }

    static void put(FloatBuffer fb, float x, float y, float z, float r, float g, float b) {
        fb.put(x).put(y).put(z).put(r).put(g).put(b);
    }

    static boolean solid(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= sx || y >= sy || z >= sz) return false;
        return world[idx(x, y, z)] != 0;
    }

    static int idx(int x, int y, int z) { return (y * sz + z) * sx + x; }

    static void buildShaders() {
        String vs = "#version 330 core\n"
            + "layout(location=0) in vec3 pos; layout(location=1) in vec3 col;"
            + "uniform mat4 mvp; out vec3 vcol;"
            + "void main(){ vcol=col; gl_Position=mvp*vec4(pos,1.0); }";
        String fs = "#version 330 core\n"
            + "in vec3 vcol; out vec4 frag;"
            + "void main(){ frag=vec4(vcol,1.0); }";
        program = GL20.glCreateProgram();
        int v = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(v, vs); GL20.glCompileShader(v);
        int f = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(f, fs); GL20.glCompileShader(f);
        GL20.glAttachShader(program, v); GL20.glAttachShader(program, f);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE)
            throw new IllegalStateException("shader link: " + GL20.glGetProgramInfoLog(program));
        mvpUniform = GL20.glGetUniformLocation(program, "mvp");
    }

    static FloatBuffer matrix(float fovDeg, float aspect) {
        try (MemoryStack st = MemoryStack.stackPush()) {
            FloatBuffer m = st.callocFloat(16);
            float fov = (float) Math.toRadians(fovDeg), near = 0.1f, far = 600f;
            float f = (float) (1.0 / Math.tan(fov / 2));
            m.put(f / aspect).put(0).put(0).put(0)
             .put(0).put(f).put(0).put(0)
             .put(0).put(0).put((far + near) / (near - far)).put(-1)
             .put(0).put(0).put(2 * far * near / (near - far)).put(0);
            m.flip();
            float[] p = new float[16];
            m.get(p);
            float cy = (float) Math.cos(Math.toRadians(yaw)), sy_ = (float) Math.sin(Math.toRadians(yaw));
            float cp = (float) Math.cos(Math.toRadians(pitch)), sp = (float) Math.sin(Math.toRadians(pitch));
            // view basis
            float rxx = cy, rxz = -sy_;
            float uy = cp, uyx = sy_ * sp, uzx = cy * sp;
            float fzy = -sp, fzx = -sy_ * cp, fzz = -cy * cp;
            float tx = -(rxz * camZ + rxx * camX);
            float ty = -(uyx * camX + uy * camY + uzx * camZ);
            float tz = -(fzx * camX + fzy * camY + fzz * camZ);
            FloatBuffer out = MemoryUtil.memAllocFloat(16);
            out.put(new float[]{
                rxx, sp * sy_, fzx, 0,
                0, cp, fzy, 0,
                rxz, sp * cy, fzz, 0,
                tx, ty, tz, 1});
            out.flip();
            // out = proj * view (single combined: multiply p * view)
            FloatBuffer res = MemoryUtil.memAllocFloat(16);
            mul(p, out, res);
            MemoryUtil.memFree(out);
            return res;
        }
    }

    static void mul(float[] a, FloatBuffer b, FloatBuffer out) {
        float[] bo = new float[16];
        b.get(bo);
        for (int c = 0; c < 4; c++)
            for (int r = 0; r < 4; r++) {
                float s = 0;
                for (int k = 0; k < 4; k++) s += a[k * 4 + r] * bo[c * 4 + k];
                out.put(c * 4 + r, s);
            }
    }
}
