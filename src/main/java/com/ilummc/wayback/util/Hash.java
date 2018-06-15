package com.ilummc.wayback.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Hash {

    private static HashFunction func = Hashing.murmur3_128();

    public static long hashFile(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            Hasher hasher = func.newHasher();
            byte[] buf = new byte[512];
            int len;
            while ((len = stream.read(buf)) > 0) hasher.putBytes(buf, 0, len);
            return hasher.hash().padToLong();
        } catch (IOException e) {
            return 0;
        }
    }
}
