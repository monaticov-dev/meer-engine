package net.meer.boot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Test client: sends MEER-PING frame, prints MEER-PONG. Usage: BootPing [host] [port] */
public final class BootPing {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 25577;
        long t0 = System.nanoTime();
        try (Socket s = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(s.getOutputStream());
             DataInputStream in = new DataInputStream(s.getInputStream())) {
            byte[] ping = "MEER-PING".getBytes(StandardCharsets.UTF_8);
            out.writeInt(ping.length);
            out.write(ping);
            out.flush();
            int len = in.readInt();
            byte[] buf = new byte[len];
            in.readFully(buf);
            long rtt = (System.nanoTime() - t0) / 1_000;
            System.out.println("reply=" + new String(buf, StandardCharsets.UTF_8) + " rtt_us=" + rtt);
        }
    }
}
