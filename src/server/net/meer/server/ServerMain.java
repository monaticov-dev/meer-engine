package net.meer.server;

import net.meer.proto.Proto;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * MEER own authoritative server, protocol v1 (M1 core).
 * World: 128x64x128 voxel bytes, server-generated terrain (server is truth, D-002).
 * On connect: handshake -> WELCOME(dims) -> full world CHUNK.
 * Server binds 0.0.0.0 (D-006) and prints the JOIN address for internet play.
 */
public final class ServerMain {
    static final int SX = 128, SY = 64, SZ = 128;
    static final byte[] WORLD = new byte[SX * SY * SZ];

    public static void main(String[] args) throws Exception {
        int port = 25577;
        String publicIp = null;
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) port = Integer.parseInt(args[++i]);
            if ("--public-ip".equals(args[i]) && i + 1 < args.length) publicIp = args[++i];
        }

        generateTerrain();
        System.out.println("[world] generated " + SX + "x" + SY + "x" + SZ);

        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        while (nis.hasMoreElements()) {
            NetworkInterface ni = nis.nextElement();
            if (!ni.isUp() || ni.isLoopback()) continue;
            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress a = addrs.nextElement();
                if (a instanceof Inet4Address) System.out.println("[net] lan_ip=" + a.getHostAddress());
            }
        }

        ServerSocket ss = new ServerSocket(port, 64, Inet4Address.getByAddress(new byte[]{0, 0, 0, 0}));
        System.out.println("[net] listening_on=0.0.0.0:" + port);
        System.out.println("[net] JOIN_ADDRESS=" + (publicIp != null ? publicIp : "<--public-ip>") + ":" + port);

        while (true) {
            Socket s = ss.accept();
            Thread.ofVirtual().name("conn-" + s.getRemoteSocketAddress()).start(() -> serve(s));
        }
    }

    static void generateTerrain() {
        for (int y = 0; y < SY; y++)
            for (int z = 0; z < SZ; z++)
                for (int x = 0; x < SX; x++) {
                    int h = 24 + (int) (6 * Math.sin(x * 0.08) + 6 * Math.cos(z * 0.08) + 2 * Math.sin((x + z) * 0.03));
                    byte id = 0;
                    if (y < h - 6) id = 3;      // stone
                    else if (y < h - 1) id = 2; // dirt
                    else if (y == h - 1) id = 1; // grass
                    WORLD[idx(x, y, z)] = id;
                }
    }

    static int idx(int x, int y, int z) { return (y * SZ + z) * SX + x; }

    static void serve(Socket s) {
        System.out.println("[conn] open " + s.getRemoteSocketAddress());
        try (Socket sock = s;
             DataInputStream in = new DataInputStream(sock.getInputStream());
             DataOutputStream out = new DataOutputStream(sock.getOutputStream())) {
            byte[] hello = Proto.readFrame(in);
            if (hello.length != 8 || hello[0] != Proto.HELLO_C
                || !"MEER-C1".equals(new String(hello, 1, 7, StandardCharsets.UTF_8))) {
                System.out.println("[conn] bad hello, closing " + s.getRemoteSocketAddress());
                return;
            }
            // WELCOME: "MEER-S1" + dims
            ByteArrayOutputStream wb = new ByteArrayOutputStream(20);
            DataOutputStream wd = new DataOutputStream(wb);
            wd.write("MEER-S1".getBytes(StandardCharsets.UTF_8));
            wd.writeInt(SX); wd.writeInt(SY); wd.writeInt(SZ);
            Proto.writeFrame(out, Proto.WELCOME, wb.toByteArray());
            // full world as one CHUNK frame
            ByteArrayOutputStream cb = new ByteArrayOutputStream(SX * SY * SZ + 20);
            DataOutputStream cd = new DataOutputStream(cb);
            cd.writeInt(SX); cd.writeInt(SY); cd.writeInt(SZ);
            cd.writeInt(0); cd.writeInt(0); cd.writeInt(0);
            cd.write(WORLD);
            Proto.writeFrame(out, Proto.CHUNK, cb.toByteArray());
            System.out.println("[conn] world sent (" + (SX * SY * SZ) + " bytes) to " + s.getRemoteSocketAddress());

            while (true) {
                byte[] f = Proto.readFrame(in);
                if (f.length < 1) continue;
                byte type = f[0];
                switch (type) {
                    case Proto.PING:
                        Proto.writeFrame(out, Proto.PONG, new byte[0]);
                        break;
                    case Proto.MOVE:
                        // intent accepted, authoritative validation lands with gameplay (M1 next iteration)
                        break;
                    default:
                        System.out.println("[conn] unknown type " + type + " from " + s.getRemoteSocketAddress());
                }
            }
        } catch (EOFException | java.net.SocketException e) {
            System.out.println("[conn] closed " + s.getRemoteSocketAddress());
        } catch (Exception e) {
            System.out.println("[conn] error " + s.getRemoteSocketAddress() + ": " + e);
        }
    }
}
