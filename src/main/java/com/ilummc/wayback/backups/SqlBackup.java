package com.ilummc.wayback.backups;

import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

@SerializableAs("SqlBackup")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SqlBackup implements ConfigurationSerializable {

    String jdbcUrl;

    String user, password;

    boolean incremental;

    public static SqlBackup valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, SqlBackup.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }
}
