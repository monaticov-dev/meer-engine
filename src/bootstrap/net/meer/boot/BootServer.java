package net.meer.boot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

/**
 * MEER own server bootstrap (S1 prototype, D-006): binds 0.0.0.0, prints LAN IPs and
 * the external IP:port players should use, accepts framed connections.
 * Frame: [int BE length][payload]. Handshake: client sends "MEER-PING", server replies "MEER-PONG".
 * Public IP: from --public-ip (explicit config only; auto-detect/UPnP lands later per D-006).
 */
public final class BootServer {

    public static void main(String[] args) throws Exception {
        int port = 25577;
        String publicIp = null;
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) port = Integer.parseInt(args[++i]);
            if ("--public-ip".equals(args[i]) && i + 1 < args.length) publicIp = args[++i];
        }

        // --- LAN addresses (bound to 0.0.0.0, this list is informational) ---
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
        String join = publicIp != null ? publicIp + ":" + port
                : "<set --public-ip or UPnP (D-006 later)>:" + port;
        System.out.println("[net] JOIN_ADDRESS=" + join);

        while (true) {
            Socket s = ss.accept();
            Thread.ofPlatform().name("conn-" + s.getRemoteSocketAddress()).start(() -> serve(s));
        }
    }

    static void serve(Socket s) {
        System.out.println("[conn] open from " + s.getRemoteSocketAddress());
        try (Socket sock = s;
             DataInputStream in = new DataInputStream(sock.getInputStream());
             DataOutputStream out = new DataOutputStream(sock.getOutputStream())) {
            while (true) {
                int len = in.readInt();
                if (len < 0 || len > 1 << 20) throw new EOFException("bad frame len " + len); // guard (D-006 anti-garbage)
                byte[] buf = new byte[len];
                in.readFully(buf);
                String msg = new String(buf, java.nio.charset.StandardCharsets.UTF_8);
                if ("MEER-PING".equals(msg)) {
                    byte[] pong = "MEER-PONG".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    out.writeInt(pong.length);
                    out.write(pong);
                    out.flush();
                    System.out.println("[conn] ping->pong " + s.getRemoteSocketAddress());
                } else {
                    System.out.println("[conn] unknown frame: " + msg);
                }
            }
        } catch (Exception e) {
            System.out.println("[conn] closed " + s.getRemoteSocketAddress() + " (" + e + ")");
        }
    }
}
