package com.ilummc.wayback.tasks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.wayback.Stats;
import com.ilummc.wayback.WaybackConf;
import com.ilummc.wayback.backups.FileBackup;
import com.ilummc.wayback.compress.Archive;
import com.ilummc.wayback.compress.Compressor;
import com.ilummc.wayback.data.Breakpoint;
import com.ilummc.wayback.policy.Policy;
import com.ilummc.wayback.storage.LocalStorage;
import com.ilummc.wayback.util.WrapLong;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class IncrementalFileLocalTransferTask implements Executable {

    private String detail = "WAIT_RUNNING";

    private long eta = 0;

    private double progress = 0.0;

    private final FileBackup backup;

    private final LocalStorage storage;

    private final List<Policy> noEnoughSpace;

    private final List<Policy> complete;

    private long count = 0;

    private Task next;

    IncrementalFileLocalTransferTask next(Task task) {
        this.next = task;
        return this;
    }

    IncrementalFileLocalTransferTask(FileBackup backup, LocalStorage storage, List<Policy> noEnoughSpace, List<Policy> complete) {
        this.backup = backup;
        this.storage = storage;
        this.noEnoughSpace = noEnoughSpace;
        this.complete = complete;
    }

    @Override
    public void execute() throws Exception {
        eta = 30000;
        detail = "CREATING_FILE_MAP";
        Gson gson = new Gson();
        File base = storage.largest();
        JsonObject object = backup.makeFileInfo();
        progress += 0.1;
        JsonObject lastMap = storage.findLast().flatMap(breakpoint -> Optional.of(breakpoint.getMap())).orElse(new JsonObject());
        Map<String, Object> diff = Breakpoint.makeDiff(object, lastMap);
        progress += 0.1;

        // ensure enough disk space
        WrapLong size = new WrapLong(0), count = new WrapLong(0);
        sizeOf(size, count, object, diff);
        if (storage.space() < size.get())
            for (Policy policy : noEnoughSpace) {
                policy.accept(this);
            }
        eta = (long) (((((double) size.get()) / (10 << 20)) * 1024) + 5000);

        Breakpoint breakpoint = new Breakpoint(object, gson.toJsonTree(diff).getAsJsonObject(), true);
        this.count = count.get();
        detail = "COMPRESSING";
        progress += 0.1;
        eta -= 5000;

        // compress
        Compressor compressor = WaybackConf.getConf().getAvailableCompressor();
        Archive archive = compressor.createArchive(base, storage);
        zip("", diff, archive);
        LocalDateTime time = LocalDateTime.now();
        File zipped = archive.create(base, time);

        // make sure this file was created correctly as the move operation usually take no time
        File js = new File(base, time.toString().replace(':', '_') + ".json0");
        breakpoint.writeTo(js);
        Files.move(js.toPath(), new File(base, time.toString().replace(':', '_') + ".json").toPath());
        progress = 1.0;
        for (Policy policy : complete) {
            policy.accept(this);
        }
        reset();
        if (next != null) next.create().schedule().addToQueue();
        Stats.increaseBackup();
    }

    private void reset() {
        eta = 0;
        progress = 0;
        detail = "WAIT_RUNNING";
        count = 0;
    }

    @SuppressWarnings("unchecked")
    private void zip(String path, Map<String, Object> diff, Archive archive) throws Exception {
        for (Map.Entry<String, Object> entry : diff.entrySet()) {
            if (entry.getValue() instanceof Map) zip(path + entry.getKey() + "/", ((Map) entry.getValue()), archive);
            else if (entry.getValue() instanceof Breakpoint.Change && entry.getValue() != Breakpoint.Change.D) {
                TLocale.Logger.fine("FILE_LOCAL.ZIPPING_FILE", path + entry.getKey());
                archive.write(path + entry.getKey(), backup.getInput(path + entry.getKey()));
                progress += (1D / ((double) count)) * 0.65;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void sizeOf(WrapLong size, WrapLong count, JsonObject object, Map<String, Object> diff) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (diff.containsKey(entry.getKey()) && diff.get(entry.getKey()) != Breakpoint.Change.D) {
                if (entry.getValue().isJsonArray()) {
                    count.increment();
                    size.increment(entry.getValue().getAsJsonArray().get(1).getAsLong());
                } else {
                    sizeOf(size, count, entry.getValue().getAsJsonObject(), (Map<String, Object>) diff.get(entry.getKey()));
                }
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
        return progress;
    }

    @Override
    public long eta() {
        return eta;
    }

    @Override
    public String detail() {
        return TLocale.asString("FILE_LOCAL." + detail, String.valueOf(count));
    }

    @Override
    public String name() {
        return TLocale.asString("TASKS.FILE_LOCAL_TRANSFER_NAME");
    }

}
