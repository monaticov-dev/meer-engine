package net.meer.proto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * MEER protocol v1 (D-006): length-prefixed frames, [int BE len][byte type][payload].
 * Server is the single source of truth (D-002); client sends intent, server sends snapshots.
 */
public final class Proto {
    public static final int MAX_FRAME = 1 << 24; // 16 MiB hard cap (anti-garbage guard)

    public static final byte HELLO_C   = 1; // C->S payload "MEER-C1"
    public static final byte WELCOME   = 2; // S->C payload "MEER-S1", sx, sy, sz (ints)
    public static final byte CHUNK     = 3; // S->C sx,sy,sz, ox,oy,oz + raw block bytes (row-major x,z,y within box)
    public static final byte BLOCK_SET = 4; // S->C x,y,z,id
    public static final byte PING      = 5; // C->S
    public static final byte PONG      = 6; // S->C
    public static final byte MOVE      = 7; // C->S x,y,z,yaw,pitch (floats) — intent, server validates later

    public static byte[] readFrame(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0 || len > MAX_FRAME) throw new IOException("bad frame len " + len);
        byte[] buf = new byte[len];
        in.readFully(buf);
        return buf;
    }

    public static void writeFrame(DataOutputStream out, byte type, byte[] payload) throws IOException {
        out.writeInt(1 + payload.length);
        out.writeByte(type);
        out.write(payload);
        out.flush();
    }
}
