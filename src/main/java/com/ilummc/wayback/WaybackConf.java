package com.ilummc.wayback;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.wayback.backups.Backup;
import com.ilummc.wayback.compress.Compressor;
import com.ilummc.wayback.compress.ZipCompressor;
import com.ilummc.wayback.policy.Policy;
import com.ilummc.wayback.schedules.PreloadSchedule;
import com.ilummc.wayback.schedules.ProgressedSchedule;
import com.ilummc.wayback.storage.Storage;
import com.ilummc.wayback.tasks.Task;
import com.ilummc.wayback.util.Crypto;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WaybackConf {

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private WaybackConf(FileConfiguration configuration, boolean encrypted) {
        this.configuration = configuration;
        this.encrypted = encrypted;
    }

    private boolean encrypted;

    private Map<String, ProgressedSchedule> schedules;

    private Map<String, Storage> storages;

    private FileConfiguration configuration;

    public int getPoolSize() {
        return configuration.getInt("pool_size", Runtime.getRuntime().availableProcessors());
    }

    public Storage getStorage(String name) {
        return name != null ? storages.get(name) : null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Storage> getStorages() {
        return storages == null ? ((Map<String, Storage>) ((Map) configuration.getConfigurationSection("storages").getValues(false))) : storages;
    }

    public Compressor getAvailableCompressor() {
        return getCompressors().stream().filter(Objects::nonNull).findAny().orElse(new ZipCompressor());
    }

    @SuppressWarnings("unchecked")
    public List<Compressor> getCompressors() {
        return (List<Compressor>) configuration.getList("compressor");
    }

    public Backup getBackup(String name) {
        return name != null ? (Backup) configuration.getConfigurationSection("backups").get(name) : null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Backup> getBackups() {
        return ((Map<String, Backup>) ((Map) configuration.getConfigurationSection("backups").getValues(false)));
    }

    public Policy getPolicy(String name) {
        return name != null ? ((Policy) configuration.getConfigurationSection("policies").get(name)).create() : null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Policy> getPolicyies() {
        return ((Map<String, Policy>) ((Map) configuration.getConfigurationSection("policies").getValues(false)));
    }

    public Task getTask(String name) {
        return name != null ? ((Task) configuration.getConfigurationSection("tasks").get(name)) : null;
    }

    public Map<String, ProgressedSchedule> getSchedules() {
        return ImmutableMap.copyOf(schedules);
    }

    @SuppressWarnings("unchecked")
    private Map<String, PreloadSchedule> getPreloadSchedules() {
        return ((Map<String, PreloadSchedule>) ((Map) configuration.getConfigurationSection("schedules").getValues(false)));
    }

    private static WaybackConf conf;

    public static WaybackConf getConf() {
        return conf;
    }

    static void unencrypt(CommandSender sender) {
        if (!getConf().encrypted) throw new NullPointerException(TLocale.asString("COMMANDS.NOT_ENCRYPTED"));
        else {
            try {
                save(loadPlainText(new AtomicBoolean(false)));
                TLocale.sendTo(sender, "CONVERT_TO_UNENCRYPTED");
                getConf().encrypted = false;
            } catch (IOException ignored) {
            }
        }
    }

    static void encrypt(String password, CommandSender sender) {
        if (getConf().encrypted) throw new NullPointerException(TLocale.asString("COMMANDS.ALREADY_ENCRYPTED"));
        try {
            String content = load();
            content = Crypto.encrypt(content, password);
            save(content);
            TLocale.sendTo(sender, "CONVERT_TO_ENCRYPTED");
            getConf().encrypted = true;
        } catch (IOException e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
    }

    public static void setup() {

    }

    static void init() {
        AtomicBoolean enc = new AtomicBoolean(false);
        String content = loadPlainText(enc);
        FileConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(content);
            conf = new WaybackConf(configuration, enc.get());
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            TLocale.Logger.error("CORRUPTED_CONF");
            try {
                configuration.load(new BufferedReader(new InputStreamReader(WaybackConf.class.getResourceAsStream("/config.yml"))));
                conf = new WaybackConf(configuration, enc.get());
            } catch (IOException | InvalidConfigurationException e1) {
                TLocale.Logger.error("ERR_LOAD_DEFAULT_CONF");
                throw new NullPointerException();
            }
        }
        {
            HashMap<String, Storage> map = new HashMap<>();
            getConf().getStorages().forEach((name, storage) -> {
                if (storage != null && storage.init())
                    map.put(name, storage);
            });
            getConf().storages = map;
        }
        {
            HashMap<String, ProgressedSchedule> map = new HashMap<>();
            getConf().getPreloadSchedules().forEach((name, preload) -> {
                if (preload != null)
                    map.put(name, preload.toSchedule());
            });
            getConf().schedules = map;
        }
        getConf().getSchedules().values().forEach(ProgressedSchedule::addToQueue);
    }

    private static String loadPlainText(AtomicBoolean enc) {
        String content = null;
        try {
            content = load();
            if (!content.contains("_")) {
                enc.set(true);
                TLocale.Logger.info("ENCRYPTED_CONF");
                Optional<Thread> any = Thread.getAllStackTraces().keySet().stream()
                        .filter(thread -> "Server console handler".equals(thread.getName()))
                        .findAny();
                any.ifPresent(Thread::suspend);
                try {
                    String decrypted = "";
                    while (!decrypted.contains("_")) {
                        String key = reader.readLine();
                        if ("!!".equals(key)) {
                            content = cleanThenLoad();
                            enc.set(false);
                            TLocale.Logger.info("USING_DEFAULT_CONF");
                            break;
                        }
                        decrypted = Crypto.decrypt(content, key);
                        if (decrypted.contains("_")) {
                            TLocale.Logger.info("DECRYPTED");
                            content = decrypted;
                            break;
                        }
                        TLocale.Logger.warn("INCORRECT_PASSWORD");
                    }
                } catch (Throwable ignored) {
                } finally {
                    any.ifPresent(Thread::resume);
                }
            }
        } catch (IOException e) {
            TLocale.Logger.error("ERR_LOAD_CONF");
        }
        return content;
    }

    private static String readPassword() {
        Optional<Thread> any = Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> "Server console handler".equals(thread.getName()))
                .findAny();
        any.ifPresent(Thread::suspend);
        try {
            return reader.readLine();
        } catch (Throwable ignored) {
            return "";
        } finally {
            any.ifPresent(Thread::resume);
        }
    }

    private static String cleanThenLoad() throws IOException {
        File config = new File(Wayback.instance().getDataFolder(), "config.yml");
        if (config.exists()) if (!config.delete()) throw new IOException();
        return load();
    }

    private static String load() throws IOException {
        File config = new File(Wayback.instance().getDataFolder(), "config.yml");
        if (!config.exists()) Wayback.instance().saveResource("config.yml", true);
        return Files.toString(config, Charset.forName("utf-8"));
    }

    private static void save(String content) throws IOException {
        File config = new File(Wayback.instance().getDataFolder(), "config.yml");
        Files.write(content.getBytes("utf-8"), config);
    }

}
