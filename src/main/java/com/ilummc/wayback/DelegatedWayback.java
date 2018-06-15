package com.ilummc.wayback;

import com.ilummc.tlib.inject.TPluginManager;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.wayback.backups.FileBackup;
import com.ilummc.wayback.backups.SqlBackup;
import com.ilummc.wayback.cmd.CommandRegistry;
import com.ilummc.wayback.compress.ZipCompressor;
import com.ilummc.wayback.compress.SevenZipCompressor;
import com.ilummc.wayback.compress.XzCompressor;
import com.ilummc.wayback.schedules.ProgressedSchedule;
import com.ilummc.wayback.storage.FtpStorage;
import com.ilummc.wayback.storage.LocalStorage;
import com.ilummc.wayback.policy.AbandonPolicy;
import com.ilummc.wayback.policy.CleanLatestPolicy;
import com.ilummc.wayback.policy.CleanOldestPolicy;
import com.ilummc.wayback.policy.RetryPolicy;
import com.ilummc.wayback.schedules.PreloadSchedule;
import com.ilummc.wayback.storage.Storage;
import com.ilummc.wayback.tasks.TransferTask;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

final class DelegatedWayback {

    static void onEnable(Wayback wayback) {
        TLocale.sendToConsole("LOGO", wayback.getDescription().getVersion());
        try {
            SevenZip.initSevenZipFromPlatformJAR();
        } catch (SevenZipNativeInitializationException e) {
            TLocale.Logger.fatal("ERR_LOAD_7ZIP");
        }
        registerSerializable();
        WaybackConf.init();
        WaybackConf.getConf().getStorages().values().forEach(Storage::init);
        WaybackConf.getConf().getSchedules().values().forEach(ProgressedSchedule::addToQueue);
        CommandRegistry.init();
        CommandRegistry.register(new WaybackCommand());
        new Metrics(wayback);
        TPluginManager.delayDisable(wayback);
    }

    private static void registerSerializable() {
        ConfigurationSerialization.registerClass(FileBackup.class, "File");
        ConfigurationSerialization.registerClass(FileBackup.class, "FileBackup");
        ConfigurationSerialization.registerClass(SqlBackup.class, "SQL");
        ConfigurationSerialization.registerClass(SqlBackup.class, "SqlBackup");
        ConfigurationSerialization.registerClass(SevenZipCompressor.class, "7z");
        ConfigurationSerialization.registerClass(SevenZipCompressor.class, "7zip");
        ConfigurationSerialization.registerClass(XzCompressor.class, "xz");
        ConfigurationSerialization.registerClass(ZipCompressor.class, "zip");
        ConfigurationSerialization.registerClass(FtpStorage.class, "FtpStorage");
        ConfigurationSerialization.registerClass(LocalStorage.class, "LocalStorage");
        ConfigurationSerialization.registerClass(AbandonPolicy.class, "Abandon");
        ConfigurationSerialization.registerClass(CleanOldestPolicy.class, "CleanOldest");
        ConfigurationSerialization.registerClass(CleanLatestPolicy.class, "CleanLatest");
        ConfigurationSerialization.registerClass(RetryPolicy.class, "Retry");
        ConfigurationSerialization.registerClass(TransferTask.class, "Transfer");
        ConfigurationSerialization.registerClass(PreloadSchedule.NormalPreload.class, "Instant");
        ConfigurationSerialization.registerClass(PreloadSchedule.PeriodPreload.class, "Period");
        ConfigurationSerialization.registerClass(PreloadSchedule.DelayedPreload.class, "Delayed");
    }

    static void onDisable(Wayback wayback) {
        TLocale.sendToConsole("LOGO", wayback.getDescription().getVersion());
    }

}
