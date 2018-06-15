package com.ilummc.wayback.policy;

import com.ilummc.wayback.WaybackException;
import com.ilummc.wayback.tasks.Executable;
import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbandonPolicy implements ConfigurationSerializable, Policy {

    public static AbandonPolicy valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, AbandonPolicy.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    @Override
    public void accept(Executable task) {
        throw new WaybackException("Abandoned.");
    }

    @Override
    public Policy create() {
        return this;
    }

    @Override
    public void reset() {
    }

}
