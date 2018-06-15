package com.ilummc.wayback.storage;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParser;
import com.ilummc.wayback.data.Breakpoint;
import com.ilummc.wayback.util.Files;
import com.ilummc.wayback.util.Jsons;
import com.ilummc.wayback.util.Pair;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalStorage implements ConfigurationSerializable, Storage {

    private List<String> root = ImmutableList.of();

    public static LocalStorage valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, LocalStorage.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    @Override
    public void init() {
        root.stream().map(File::new).forEach(File::mkdirs);
    }

    @Override
    public long space() {
        return largest().getFreeSpace();
    }

    @Override
    public File createTempFile(File base, String suffix) throws IOException {
        return File.createTempFile("Wayback-" + UUID.randomUUID().toString(), suffix, base);
    }

    @Override
    public Optional<Breakpoint> findLast() {
        return root.stream()
                .map(File::new)
                .filter(f -> f.isDirectory() && f.listFiles() != null)
                .flatMap(f -> Arrays.stream(Objects.requireNonNull(f.listFiles())))
                .filter(f -> f.getName().endsWith(".json"))
                .map(f -> Pair.of(f, LocalDateTime.parse(f.getName().replace('_', ':')
                        .substring(0, f.getName().lastIndexOf('.')))))
                .max(Comparator.comparing(Pair::getValue))
                .flatMap(p -> Optional.of(new JsonParser().parse(Files.toJson(p.getKey()))))
                .flatMap(element -> Optional.of(Jsons.mapTo(element.getAsJsonObject(), Breakpoint.class)));
    }

    public File largest() {
        return root.stream().map(File::new).min((o1, o2) -> (int) (o1.getFreeSpace() - o2.getFreeSpace())).orElseGet(() -> {
            File file = File.listRoots()[0];
            File file1 = new File(file, "Wayback");
            file1.mkdirs();
            return file1;
        });
    }

}
