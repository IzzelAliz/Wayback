package com.ilummc.wayback.tasks;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import com.ilummc.wayback.WaybackConf;
import com.ilummc.wayback.backups.Backup;
import com.ilummc.wayback.backups.FileBackup;
import com.ilummc.wayback.storage.FtpStorage;
import com.ilummc.wayback.storage.LocalStorage;
import com.ilummc.wayback.storage.Storage;
import com.ilummc.wayback.util.Jsons;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unchecked")
public class TransferTask implements Task, ConfigurationSerializable {

    private String from, to, next;

    @SerializedName("no_enough_space")
    private List<String> noEnoughSpacePolicy = ImmutableList.of();

    @SerializedName("connection_fail")
    private List<String> connectionFailPolicy = ImmutableList.of();

    @SerializedName("on_complete")
    private List<String> completePolicy = ImmutableList.of();

    @Override
    public Map<String, Object> serialize() {
        return new ObjectMapper().convertValue(this, Map.class);
    }

    public static TransferTask valueOf(Map<String, Object> map) {
        return Jsons.mapTo(map, TransferTask.class);
    }

    @Override
    public Executable create() {
        Storage storageFrom = WaybackConf.getConf().getStorage(from);
        Storage storageTo = WaybackConf.getConf().getStorage(to);
        Backup backupFrom = WaybackConf.getConf().getBackup(from);
        Backup backupTo = WaybackConf.getConf().getBackup(to);
        if (backupFrom instanceof FileBackup && storageTo instanceof LocalStorage) {
            if (((FileBackup) backupFrom).isIncremental())
                return new IncrementalFileLocalTransferTask(((FileBackup) backupFrom), ((LocalStorage) storageTo),
                        noEnoughSpacePolicy.stream().map(WaybackConf.getConf()::getPolicy).filter(Objects::nonNull).collect(Collectors.toList()),
                        completePolicy.stream().map(WaybackConf.getConf()::getPolicy).filter(Objects::nonNull).collect(Collectors.toList()))
                        .next(WaybackConf.getConf().getTask(next));
            else return new FullBackupFileLocalTransferTask(((FileBackup) backupFrom), ((LocalStorage) storageTo),
                    noEnoughSpacePolicy.stream().map(WaybackConf.getConf()::getPolicy).filter(Objects::nonNull).collect(Collectors.toList()),
                    completePolicy.stream().map(WaybackConf.getConf()::getPolicy).filter(Objects::nonNull).collect(Collectors.toList()))
                    .next(WaybackConf.getConf().getTask(next));
        }
        if (storageFrom instanceof LocalStorage && storageTo instanceof FtpStorage) {
            return new FtpUploadTransferTask(((LocalStorage) storageFrom), ((FtpStorage) storageTo),
                    connectionFailPolicy.stream().map(WaybackConf.getConf()::getPolicy).filter(Objects::nonNull).collect(Collectors.toList()),
                    completePolicy.stream().map(WaybackConf.getConf()::getPolicy).filter(Objects::nonNull).collect(Collectors.toList()))
                    .next(WaybackConf.getConf().getTask(next));
        }
        return null;
    }

    @Override
    public String detail() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }
}
