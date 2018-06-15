package com.ilummc.wayback.storage;

import com.ilummc.wayback.data.Breakpoint;
import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FtpStorage implements ConfigurationSerializable, Storage {

    private String url, user, password;

    public static FtpStorage valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, FtpStorage.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    @Override
    public void init() {

    }

    @Override
    public long space() {
        return 0;
    }

    @Override
    public File createTempFile(File base, String suffix) {
        return null;
    }

    @Override
    public Optional<Breakpoint> findLast() {
        return Optional.empty();
    }

}
