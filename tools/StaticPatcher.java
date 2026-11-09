import cpw.mods.fml.repackage.com.nothome.delta.GDiffPatcher;
import org.tukaani.xz.LZMAInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.Adler32;

/**
 * Static client patcher: bakes FML binpatches + forge universal into one launchwrapper-free jar.
 * Replicates ClassPatchManager.applyPatch semantics offline (LZMA -> pack200 -> GDiff).
 * Runs on Java 8 (Pack200 was removed in 14+).
 */
public class StaticPatcher {

    static final class Patch {
        String name, src, tgt;
        boolean exists;
        int checksum;
        byte[] patch;
    }

    public static void main(String[] args) throws Exception {
        File universalDir = new File(args[0]);   // unpacked forge universal
        File vanillaJar  = new File(args[1]);    // clean obfuscated client jar
        File outJar      = new File(args[2]);
        String side      = args.length > 3 ? args[3] : "client";

        // --- 1. binpatches.pack.lzma -> pack200 -> jar -> patches ---
        Map<String, List<Patch>> patches = new LinkedHashMap<String, List<Patch>>();
        int rawPatchCount = 0;
        {
            InputStream in = new FileInputStream(new File(universalDir, "binpatches.pack.lzma"));
            LZMAInputStream lz = new LZMAInputStream(in);
            ByteArrayOutputStream jarBytes = new ByteArrayOutputStream(1 << 20);
            JarOutputStream jos = new JarOutputStream(jarBytes);
            Pack200.newUnpacker().unpack(lz, jos);
            jos.close();
            JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarBytes.toByteArray()));
            JarEntry e;
            while ((e = jis.getNextJarEntry()) != null) {
                if (!e.getName().matches("binpatch/" + side + "/.*\\.binpatch")) {
                    jis.closeEntry();
                    continue;
                }
                DataInput d = new DataInputStream(new ByteArrayInputStream(readAllNoClose(jis)));
                Patch p = new Patch();
                p.name = d.readUTF();
                p.src = d.readUTF();
                p.tgt = d.readUTF();
                p.exists = d.readBoolean();
                p.checksum = p.exists ? d.readInt() : 0;
                int len = d.readInt();
                p.patch = new byte[len];
                d.readFully(p.patch);
                List<Patch> list = patches.get(p.src);
                if (list == null) { list = new ArrayList<Patch>(); patches.put(p.src, list); }
                list.add(p);
                rawPatchCount++;
            }
            jis.close();
        }
        System.out.println("binpatch entries: " + rawPatchCount + ", distinct classes: " + patches.size());

        // --- 2. load vanilla jar ---
        JarFile vj = new JarFile(vanillaJar);
        Map<String, byte[]> out = new LinkedHashMap<String, byte[]>();
        Set<String> vanillaNames = new HashSet<String>();
        for (Enumeration<JarEntry> en = vj.entries(); en.hasMoreElements(); ) {
            JarEntry je = en.nextElement();
            if (je.isDirectory()) continue;
            String n = je.getName();
            byte[] data = readAllNoClose(vj.getInputStream(je));
            out.put(n, data);
            vanillaNames.add(n);
        }
        vj.close();
        System.out.println("vanilla entries: " + out.size());

        // --- 3. apply patches ---
        GDiffPatcher gp = new GDiffPatcher();
        int patchedCount = 0, newCount = 0, checkFail = 0, skippedLib = 0;
        for (Map.Entry<String, List<Patch>> me : patches.entrySet()) {
            String obf = me.getKey();
            String entryName = obf + ".class";
            byte[] cur = out.get(entryName);
            if (cur == null) {
                // class not in vanilla jar (library-resident, e.g. realms) — fork cuts these paths anyway
                System.out.println("SKIP (lib-resident): " + obf);
                skippedLib++;
                continue;
            }
            for (Patch p : me.getValue()) {
                if (p.exists) {
                    if (cur == null) {
                        System.out.println("WARN: no vanilla class for patch " + obf + ", skipping");
                        checkFail++;
                        cur = new byte[0];
                    } else {
                        Adler32 ad = new Adler32();
                        ad.update(cur);
                        if ((int) ad.getValue() != p.checksum) {
                            System.out.println("WARN: checksum mismatch " + obf + " (disk " + Integer.toHexString((int) ad.getValue())
                                + " patch " + Integer.toHexString(p.checksum) + "), applying anyway");
                            checkFail++;
                        }
                    }
                } else {
                    cur = new byte[0];
                }
                cur = gp.patch(cur, p.patch);
            }
            if (!vanillaNames.contains(entryName)) newCount++;
            else patchedCount++;
            out.put(entryName, cur);
        }
        System.out.println("patched vanilla classes: " + patchedCount + ", forge-added classes: " + newCount
            + ", warnings: " + checkFail + ", skipped lib-resident: " + skippedLib);

        // --- 4. overlay universal entries (forge wins over vanilla) ---
        int overlay = 0;
        Queue<File> q = new LinkedList<File>(Arrays.asList(universalDir.listFiles()));
        while (!q.isEmpty()) {
            File f = q.poll();
            if (f.isDirectory()) { q.addAll(Arrays.asList(f.listFiles())); continue; }
            String rel = universalDir.toURI().relativize(f.toURI()).getPath();
            if (rel.startsWith("META-INF/")) continue;
            if (rel.equals("binpatches.pack.lzma") || rel.equals("deobfuscation_data-1.7.10.lzma")) continue;
            byte[] data = readAll(new FileInputStream(f));
            out.put(rel, data);
            overlay++;
        }
        System.out.println("universal entries overlayed: " + overlay);

        // --- 5. write ---
        JarOutputStream o = new JarOutputStream(new FileOutputStream(outJar));
        for (Map.Entry<String, byte[]> me : out.entrySet()) {
            o.putNextEntry(new JarEntry(me.getKey()));
            o.write(me.getValue());
        }
        o.close();
        System.out.println("OK wrote " + outJar.getAbsolutePath() + " entries=" + out.size());
    }

    static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream(1 << 14);
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) > 0) b.write(buf, 0, r);
        in.close();
        return b.toByteArray();
    }

    static byte[] readAllNoClose(InputStream in) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream(1 << 14);
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) > 0) b.write(buf, 0, r);
        return b.toByteArray();
    }
}
