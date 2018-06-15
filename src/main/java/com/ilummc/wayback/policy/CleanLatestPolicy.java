package com.ilummc.wayback.policy;

import com.ilummc.wayback.tasks.Executable;
import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CleanLatestPolicy implements Policy, ConfigurationSerializable {

    public static CleanLatestPolicy valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, CleanLatestPolicy.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    @Override
    public void accept(Executable task) {

    }

    @Override
    public Policy create() {
        return this;
    }

    @Override
    public void reset() {

    }

}
