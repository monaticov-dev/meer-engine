import org.tukaani.xz.LZMAInputStream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * S0 tool.
 * Input joined.srg = MCP compact format (deobfuscation_data-1.7.10.lzma):
 *   PK: notchPkg seargePkg
 *   CL: notchClass seargeClass
 *   FD: notchOwner/notchField seargeOwner/seargeField
 *   MD: notchOwner/notchMethod notchSig seargeOwner/seargeMethod seargeSig
 * Output deobf.srg = classic arrow format (SpecialSource/Vineflower compatible):
 *   PK: a -> b / CL: a -> b / FD: notch -> human / MD: notch notchSig -> human searSig
 */
public class SrgTools {

    public static void main(String[] args) throws Exception {
        String mode = args[0];
        if (mode.equals("unlzma")) {
            try (LZMAInputStream in = new LZMAInputStream(new FileInputStream(args[1]));
                 FileOutputStream out = new FileOutputStream(args[2])) {
                byte[] buf = new byte[1 << 16];
                int r;
                while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
            }
            System.out.println("unlzma OK -> " + args[2]);
            return;
        }
        if (mode.equals("mksrg")) {
            // args: mksrg <joined.srg> <fields.csv> <methods.csv> <out.srg>
            Map<String, String> ren = new HashMap<>();
            loadCsv(args[2], ren);
            loadCsv(args[3], ren);
            System.out.println("csv renames loaded: " + ren.size());

            int cl = 0, fd = 0, md = 0, pk = 0, skip = 0;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]), StandardCharsets.UTF_8));
                 BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[4]), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] t = line.split("\\s+");
                    switch (t[0]) {
                        case "PK:": {
                            if (t.length < 3) { skip++; continue; }
                            w.write("PK: " + t[1] + " -> " + t[2]); w.newLine();
                            pk++;
                            break;
                        }
                        case "CL:": {
                            if (t.length < 3) { skip++; continue; }
                            String notch = t[1], sear = t[2];
                            w.write("CL: " + notch + " -> " + sear); w.newLine();
                            cl++;
                            break;
                        }
                        case "FD:": {
                            if (t.length < 3) { skip++; continue; }
                            String notch = t[1], sear = t[2];
                            String human = ren.getOrDefault(sear.substring(sear.lastIndexOf('/') + 1), sear);
                            String humanFull = notch.substring(0, notch.lastIndexOf('/') + 1)
                                    + human.substring(human.lastIndexOf('/') + 1);
                            w.write("FD: " + notch + " -> " + humanFull); w.newLine();
                            fd++;
                            break;
                        }
                        case "MD:": {
                            if (t.length < 5) { skip++; continue; }
                            String notchName = t[1], notchSig = t[2], searName = t[3], searSig = t[4];
                            String human = ren.getOrDefault(searName.substring(searName.lastIndexOf('/') + 1), searName);
                            String humanFull = notchName.substring(0, notchName.lastIndexOf('/') + 1)
                                    + human.substring(human.lastIndexOf('/') + 1);
                            w.write("MD: " + notchName + " " + notchSig + " -> " + humanFull + " " + searSig); w.newLine();
                            md++;
                            break;
                        }
                        default:
                            skip++;
                    }
                }
                System.out.println("deobf.srg written: CL=" + cl + " FD=" + fd + " MD=" + md + " PK=" + pk + " skip=" + skip + " -> " + args[4]);
            }
            return;
        }
        throw new IllegalArgumentException("mode: unlzma|mksrg");
    }

    static void loadCsv(String path, Map<String, String> ren) throws Exception {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            int i = 0;
            while ((line = r.readLine()) != null) {
                if (i++ == 0) continue; // header
                int c1 = line.indexOf(',');
                if (c1 <= 0) continue;
                int c2 = line.indexOf(',', c1 + 1);
                if (c2 < 0) continue;
                String searge = line.substring(0, c1).trim();
                String name = line.substring(c1 + 1, c2).trim();
                if (!searge.isEmpty() && !name.isEmpty()) ren.put(searge, name);
            }
        }
    }
}
