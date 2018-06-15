package com.ilummc.wayback.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Files {

    public static String toJson(File file) {
        try {
            return com.google.common.io.Files.toString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "{}";
        }
    }
}
