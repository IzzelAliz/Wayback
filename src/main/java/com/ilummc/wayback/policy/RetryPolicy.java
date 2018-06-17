package com.ilummc.wayback.policy;

import com.google.gson.annotations.SerializedName;
import com.ilummc.wayback.WaybackException;
import com.ilummc.wayback.tasks.Executable;
import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RetryPolicy implements ConfigurationSerializable, Policy {

    @SerializedName("max_retry")
    private int maxRetry = 3;

    @SerializedName("retry_period")
    private int retryPeriod = 60;

    public static RetryPolicy valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, RetryPolicy.class);
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
        return new SimpleRetry(maxRetry);
    }

    @Override
    public void reset() {
    }

    private class SimpleRetry extends RetryPolicy {

        private int times;

        SimpleRetry(int times) {
            this.times = times;
        }

        @Override
        public void accept(Executable task) {
            if (this.times-- > 0) {
                throw new WaybackException();
            } else throw new NullPointerException();
        }

        @Override
        public void reset() {
            times = maxRetry;
        }
    }

}
