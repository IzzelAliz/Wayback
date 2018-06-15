package com.ilummc.wayback.compress;

import com.ilummc.wayback.storage.Storage;
import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SevenZipCompressor implements ConfigurationSerializable, Compressor {

    private boolean encrypt = false;

    private int level;

    private String password;

    public static SevenZipCompressor valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, SevenZipCompressor.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    @Override
    public Archive createArchive(File base, Storage storage) throws IOException {
        return new SevenZipArchive(storage.createTempFile(base, ".7z"));
    }

    @Override
    public String suffix() {
        return "7z";
    }

    private class SevenZipArchive implements Archive {

        private SevenZipArchive(File file) throws IOException {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

        }

        @Override
        public void write(String name, InputStream input) {

        }

        @Override
        public File create(File base, LocalDateTime time) {
            return null;
        }

    }
}
