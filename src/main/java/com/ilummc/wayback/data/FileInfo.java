package com.ilummc.wayback.data;

import com.google.gson.annotations.SerializedName;

public class FileInfo {

    @SerializedName("p")
    private final String relativePath;

    @SerializedName("i")
    final long[] info = new long[3];

    public FileInfo() {
        this(".", 0, 0, 0);
    }

    public FileInfo(String relativePath, long crc32, long size, long lastModified) {
        this.relativePath = relativePath;
        info[0] = crc32;
        info[1] = size;
        info[2] = lastModified;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public long getCrc32() {
        return info[0];
    }

    public long getSize() {
        return info[1];
    }

    public long getLastModified() {
        return info[2];
    }

}
