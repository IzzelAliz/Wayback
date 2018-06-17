package com.ilummc.wayback.backups;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonWriter;
import com.ilummc.tlib.util.Strings;
import com.ilummc.wayback.util.Hash;
import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@SerializableAs("File")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileBackup implements ConfigurationSerializable, Backup {

    private String root = "./";

    private File base;

    private List<String> excludes = ImmutableList.of("WaybackBackups", "logs");

    @SerializedName("skip_large_file")
    private boolean skipLargeFile = true;

    @SerializedName("large_file")
    private double largeFile = 64.000D;

    private boolean incremental = true;

    public boolean isIncremental() {
        return incremental;
    }

    public boolean isSkipLargeFile() {
        return skipLargeFile;
    }

    public long getLargeFile() {
        long l = (long) (largeFile * 1024 * 1024);
        return l < 0 ? Long.MAX_VALUE : l;
    }

    private File getBase() {
        return base == null ? base = new File(root) : base;
    }

    public InputStream getInput(String path) throws IOException {
        return new FileInputStream(new File(getBase(), path));
    }

    public JsonObject makeFileInfo() {
        File base = getBase();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "utf-8"))) {
            writer.beginObject();
            File[] list = base.listFiles();
            if (list != null) {
                for (File file : list) {
                    if (!excludes.contains(file.getName())) {
                        if (file.isDirectory()) {
                            writer.name(file.getName()).beginObject();
                            eachMake(file, writer);
                            writer.endObject();
                        } else makeSingle(file, file.getName(), writer);
                    }
                }
            }
            writer.endObject();
            writer.flush();
            return new JsonParser().parse(out.toString("utf-8")).getAsJsonObject();
        } catch (IOException e) {
            return new JsonObject();
        }
    }

    private void eachMake(File base, JsonWriter writer) throws IOException {
        if (!base.isDirectory()) {
            makeSingle(base, base.getName(), writer);
            return;
        }
        String[] list = base.list();
        if (list != null) {
            for (String s : list) {
                File file = new File(base, s);
                if (file.isDirectory()) {
                    writer.name(s).beginObject();
                    eachMake(file, writer);
                    writer.endObject();
                } else makeSingle(file, s, writer);
            }
        }
    }

    private void makeSingle(File file, String s, JsonWriter writer) throws IOException {
        if (file.length() < getLargeFile())
            writer.name(s).beginArray()
                    .value(Hash.hashFile(file)).value(file.length())
                    .endArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    public static FileBackup valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, FileBackup.class);
    }

}
