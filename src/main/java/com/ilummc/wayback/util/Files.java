package com.ilummc.wayback.util;

import io.izzel.taboolib.module.locale.TLocale;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Files {

    /**
     * 擦屁股的工具类（
     *
     * @author sandtechnology
     */
    public static String readJson(File file) {
        String result = read(file, StandardCharsets.UTF_8);
        return result.equals("") ? "{}" : result;
    }

    public static String read(File file, Charset charsets) {
        try {
            return String.join("\n", java.nio.file.Files.readAllLines(file.toPath(), charsets));
        } catch (IOException e) {
            TLocale.Logger.error("FILE_READ_ERROR");
            e.printStackTrace();
            return "";
        }
    }

    public static void write(String content, File target, Charset charset) {
        try {
            java.nio.file.Files.write(target.toPath(), content.getBytes(charset));
        } catch (IOException e) {
            TLocale.Logger.error("FILE_WRITE_ERROR");
            e.printStackTrace();
        }
    }
}
