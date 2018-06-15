package com.ilummc.wayback.data;

import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Breakpoint {

    private boolean incremental;

    private JsonObject map;

    private JsonObject changes;

    public Breakpoint(JsonObject map, JsonObject changes, boolean incremental) {
        this.map = map;
        this.changes = changes;
        this.incremental = incremental;
    }

    public JsonObject getMap() {
        return map;
    }

    public void setMap(JsonObject map) {
        this.map = map;
    }

    public JsonObject getChanges() {
        return changes;
    }

    public void setChanges(JsonObject changes) {
        this.changes = changes;
    }

    public void writeTo(File file) throws IOException {
        com.google.common.io.Files.write(new GsonBuilder().disableHtmlEscaping().create().toJson(this).getBytes(StandardCharsets.UTF_8), file);
    }

    public boolean isIncremental() {
        return incremental;
    }

    public Breakpoint setIncremental(boolean incremental) {
        this.incremental = incremental;
        return this;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> removeEmpty(Map<String, Object> map) {
        map.entrySet().removeIf(entry -> entry.getValue() instanceof Map && removeEmpty(((Map) entry.getValue())).isEmpty());
        return map;
    }

    public static Map<String, Object> makeDiff(JsonObject cur, JsonObject prev) {
        HashMap<String, Object> map = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : cur.entrySet()) {
            if (entry.getValue().isJsonArray()) {
                long hash = entry.getValue().getAsJsonArray().get(0).getAsLong();
                if (prev.has(entry.getKey())) {
                    if (prev.get(entry.getKey()).isJsonArray()) {
                        long prevHash = prev.getAsJsonArray(entry.getKey()).get(0).getAsLong();
                        if (hash != prevHash)
                            map.put(entry.getKey(), Change.M);
                    } else map.put(entry.getKey(), Change.M);
                } else map.put(entry.getKey(), Change.C);
            } else if (entry.getValue().isJsonObject()) {
                if (prev.has(entry.getKey()) && prev.get(entry.getKey()).isJsonArray())
                    map.put(entry.getKey(), Change.D);
                else map.put(entry.getKey(), makeDiff(entry.getValue().getAsJsonObject(),
                        prev.has(entry.getKey()) ? prev.getAsJsonObject(entry.getKey()) : new JsonObject()));
            }
        }
        for (Map.Entry<String, JsonElement> entry : prev.entrySet()) {
            if (!cur.has(entry.getKey())) map.put(entry.getKey(), Change.D);
        }
        return removeEmpty(map);
    }

    public enum Change {
        M, D, C
    }
}
