package com.ilummc.wayback;

import com.google.common.io.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Stats {

    private static YamlConfiguration configuration = new YamlConfiguration();

    private static File file = new File(Wayback.instance().getDataFolder(), "stats.yml");

    static void init() {
        if (!file.exists()) Wayback.instance().saveResource("stats.yml", true);
        try {
            configuration.loadFromString(Files.toString(file, StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException ignored) {
        }
    }

    public static void increaseBackup() {
        configuration.set("backup_count", configuration.getInt("backup_count", 0) + 1);
        try {
            Files.write(configuration.saveToString(), file, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static void increaseRecovery() {
        configuration.set("recovery_count", configuration.getInt("recovery_count", 0) + 1);
        try {
            Files.write(configuration.saveToString(), file, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static int getBackups() {
        return configuration.getInt("backup_count", 0);
    }

    public static int getRecoveries() {
        return configuration.getInt("recovery_count", 0);
    }

}
