package com.ilummc.wayback.tasks;

import com.google.common.collect.ImmutableMap;
import com.ilummc.wayback.Stats;
import com.ilummc.wayback.Wayback;
import com.ilummc.wayback.WaybackConf;
import com.ilummc.wayback.backups.FileBackup;
import com.ilummc.wayback.compress.Archive;
import com.ilummc.wayback.compress.Compressor;
import com.ilummc.wayback.data.Breakpoint;
import com.ilummc.wayback.storage.LocalStorage;
import com.ilummc.wayback.storage.Storage;
import com.ilummc.wayback.util.Jsons;
import com.ilummc.wayback.util.Reference;
import io.izzel.taboolib.module.locale.TLocale;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class RollbackTask implements Task, ConfigurationSerializable {

    //Sync Task for rollback
    private volatile static Runnable syncTaskList;
    private volatile static CountDownLatch countDownLatch;

    private List<String> from;

    private String to;

    private Reference<String> detail = Reference.of("AWAIT_RUN");

    private static void syncTask(Runnable runnable) throws InterruptedException {
        syncTaskList = runnable;
        countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    synchronized public static void executeSyncTask() {
        if (syncTaskList != null && countDownLatch != null) {
            try {
                syncTaskList.run();
            } catch (Exception ignored) {

            } finally {
                countDownLatch.countDown();
            }
        }
    }

    @Override
    public Executable create() {
        return null;
    }

    public Executable create(LocalDateTime time) {
        if (WaybackConf.getConf().getBackup(to) instanceof FileBackup)
            return new LocalRollbackTask(time);
        else return null;
    }

    @Override
    public String detail() {
        return TLocale.asString("ROLLBACK." + detail.getValue());
    }

    @Override
    public String name() {
        return TLocale.asString("TASKS.ROLLBACK");
    }

    @Override
    public Map<String, Object> serialize() {
        return ImmutableMap.of();
    }

    public static RollbackTask valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, RollbackTask.class);
    }

    private class LocalRollbackTask extends RollbackTask implements Executable {

        private final List<LocalStorage> fromStorage;

        private final FileBackup backup;

        private final LocalDateTime time;

        private AtomicLong progress = new AtomicLong(0);

        private LocalRollbackTask(LocalDateTime time) {
            this.time = time;
            fromStorage = from.stream().map(s -> {
                Storage storage = WaybackConf.getConf().getStorage(s);
                if (storage instanceof LocalStorage) return ((LocalStorage) storage);
                else return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            backup = ((FileBackup) WaybackConf.getConf().getBackup(to));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute() throws Exception {
            System.gc();

            // unload all worlds to unlock map data files
            TLocale.sendToConsole("ROLLBACK.PREPARE_DISABLE_WORLDS");
            RollbackTask.syncTask(() ->
                    Bukkit.getWorlds().forEach(world -> {
                        try {
                            Method getHandle = world.getClass().getDeclaredMethod("getHandle");
                            getHandle.setAccessible(true);
                            Object worldServer = getHandle.invoke(world);
                            Field dimensionF = worldServer.getClass().getDeclaredField("dimension");
                            int dimension = ((int) dimensionF.get(worldServer));
                            if (dimension <= 1)
                                TLocale.Logger.warn("ROLLBACK.UNUNLOADABLE_WORLD", world.getName());
                        } catch (Exception ignored) {
                        }
                        Bukkit.unloadWorld(world, true);
                    }));

            // disable plugins
            TLocale.sendToConsole("ROLLBACK.PREPARE_DISABLE_PLUGINS");
            RollbackTask.syncTask(() ->
                    {
                        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                            if (plugin != Wayback.instance())
                                Bukkit.getPluginManager().disablePlugin(plugin);
                        }
                    }
            );

            // stop the world :D
            Thread.getAllStackTraces().keySet().stream()
                    .filter(thread -> !thread.getName().startsWith("Wayback Schedule"))
                    .forEach(Thread::stop);

            // reset the output stream because the logger thread has been stopped
            System.setOut(new PrintStream(((OutputStream) Class.forName("org.fusesource.jansi.AnsiConsole")
                    .getDeclaredMethod("wrapOutputStream", OutputStream.class)
                    .invoke(null, new FileOutputStream(FileDescriptor.out)))));

            TLocale.sendToConsole("ROLLBACK.PREPARE_DONE");
            System.gc();

            LocalStorage str = fromStorage.get(0);
            Validate.notNull(str);

            Compressor compressor = Wayback.getConf().getAvailableCompressor();
            Validate.notNull(compressor);

            fromStorage.stream().map(storage -> storage.getExactly(time))
                    .filter(Objects::nonNull)
                    .forEach(breakpoint -> {
                        TLocale.Logger.info("ROLLBACK.COLLECT");
                        Map<String, Object> diff = Breakpoint.makeDiff(backup.makeFileInfo(), breakpoint.getMap());
                        File base = new File(backup.getRoot());
                        TLocale.Logger.info("ROLLBACK.DELETE");
                        deleteFiles(diff, base);
                        List<String> list = Breakpoint.toPlain(diff);
                        diff.clear();
                        diff = null;

                        TLocale.Logger.info("ROLLBACK.WRITE");
                        fromStorage.forEach(storage -> storage.listAvailable().stream()
                                .filter(bk -> breakpoint.getTime().compareTo(bk) >= 0)
                                .forEach(prev -> {
                                    storage.findByTime(prev, compressor.suffix()).ifPresent(prevArchive -> {
                                        try {
                                            Archive from = compressor.from(prevArchive);
                                            Iterator<String> iterator = list.iterator();
                                            while (iterator.hasNext()) {
                                                String entry = iterator.next();
                                                try {
                                                    if (from.hasEntry(entry)) {
                                                        iterator.remove();
                                                        File target = new File(base, entry);
                                                        from.transferTo(entry, target);
                                                        TLocale.Logger.fine("ROLLBACK.FILE_REPLACE", target.toString());
                                                    }
                                                } catch (Exception ignored) {
                                                    TLocale.Logger.warn("ROLLBACK.WRITE_ERR", entry);
                                                }
                                            }
                                        } catch (Exception ignored) {
                                            TLocale.Logger.warn("ROLLBACK.OPEN_ERR", prevArchive.getName());
                                        }
                                    });
                                }));
                    });

            TLocale.sendToConsole("ROLLBACK.SUCCESS");
            Stats.increaseRecovery();
            Runtime.getRuntime().halt(0);
        }

        @SuppressWarnings("unchecked")
        private void deleteFiles(Map<String, Object> diff, File base) {
            for (Map.Entry<String, Object> entry : diff.entrySet()) {
                if (entry.getValue() == Breakpoint.Change.C) {
                    File file = new File(base, entry.getKey());
                    if (!file.delete()) file.deleteOnExit();
                    TLocale.Logger.fine("ROLLBACK.FILE_DELETE", file.toString());
                } else if (entry.getValue() instanceof Map) {
                    deleteFiles(((Map) entry.getValue()), new File(base, entry.getKey()));
                }
            }
        }

        @Override
        public boolean terminate() {
            return false;
        }

        @Override
        public void forceTerminate() {

        }

        @Override
        public double progress() {
            return progress.get();
        }

        @Override
        public long eta() {
            return 0;
        }
    }


}
