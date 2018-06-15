package com.ilummc.wayback.compress;

import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XzCompressor implements ConfigurationSerializable {

    public static XzCompressor valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, XzCompressor.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }
}
