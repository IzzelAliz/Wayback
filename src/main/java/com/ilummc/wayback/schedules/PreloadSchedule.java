package com.ilummc.wayback.schedules;

import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;
import com.ilummc.wayback.WaybackConf;
import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PreloadSchedule implements ConfigurationSerializable {

    @Override
    public Map<String, Object> serialize() {
        return ImmutableMap.of();
    }

    public ProgressedSchedule toSchedule() {
        if (this instanceof PeriodPreload)
            return ((PeriodPreload) this).task == null ? null :
                    PeriodSchedule.of(WaybackConf.getConf().getTask(((PeriodPreload) this).task).create(),
                            ((PeriodPreload) this).initial, ((PeriodPreload) this).period, ((PeriodPreload) this).timeUnit);
        else if (this instanceof DelayedPreload)
            return ((DelayedPreload) this).task == null ? null :
                    DelayedSchedule.of(WaybackConf.getConf().getTask(((DelayedPreload) this).task).create(),
                            ((DelayedPreload) this).delay, ((DelayedPreload) this).timeUnit);
        else if (this instanceof NormalPreload)
            return ProgressedSchedule.of(WaybackConf.getConf().getTask(((NormalPreload) this).task).create());
        else return null;
    }

    public static class NormalPreload extends PreloadSchedule {

        String task;

        public static NormalPreload valueOf(Map<String, Object> map) {
            return Jsons.mapTo(map, NormalPreload.class);
        }
    }

    public static class PeriodPreload extends PreloadSchedule {

        @SerializedName("time_unit")
        TimeUnit timeUnit;

        String task;

        long period, initial;

        public static PeriodPreload valueOf(Map<String, Object> map) {
            return Jsons.mapTo(map, PeriodPreload.class);
        }
    }

    public static class DelayedPreload extends PreloadSchedule {

        @SerializedName("time_unit")
        TimeUnit timeUnit;

        String task;

        long delay;

        public static DelayedPreload valueOf(Map<String, Object> map) {
            return Jsons.mapTo(map, DelayedPreload.class);
        }
    }
}
