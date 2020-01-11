package com.ilummc.wayback;

import com.ilummc.wayback.schedules.WaybackSchedules;
import io.izzel.taboolib.module.dependency.Dependency;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.Bukkit;


@Dependency(maven = "it.sauronsoftware:ftp4j:1.7.2", mavenRepo = "https://bkm016.github.io/TabooLib/repo")
//@Dependency(type = Dependency.Type.LIBRARY, maven = "net.sf.sevenzipjbinding:sevenzipjbinding:9.20-2.00beta")
//@Dependency(type = Dependency.Type.LIBRARY, maven = "net.sf.sevenzipjbinding:sevenzipjbinding-all-platforms:9.20-2.00beta")
@Dependency(maven = "commons-collections:commons-collections:3.2.2")
@Dependency(maven = "net.lingala.zip4j:zip4j:1.3.2")
public final class Wayback extends WaybackLibLoader {

    private TLogger logger;

    private boolean loaded = false;

    private boolean disabling = false;

    private static Wayback instance;

    public static WaybackSchedules getSchedules() {
        return WaybackSchedules.instance();
    }

    public static WaybackConf getConf() {
        return WaybackConf.getConf();
    }

    public static TLogger logger() {
        return instance().logger;
    }

    public static Wayback instance() {
        return instance;
    }

    public static boolean isDisabling() {
        return instance().disabling;
    }

    public Wayback() {

    }

    public static boolean reload() {
        try {
            instance().onStopping();
            WaybackConf.getConf().cleanSchedules();
            WaybackSchedules.renew();
            Wayback.instance().reloadConfig();
            TLocale.reload();
            instance().onStarting();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onLoading() {
        instance = this;
        logger = new TLogger("[{0}][{1}§f] {2}", instance(), TLogger.INFO);
    }

    @Override
    public void onStarting() {
        if (!loaded)
            try {
                DelegatedWayback.onEnable();
                loaded = true;
            } catch (Throwable t) {
                TLocale.Logger.fatal("ERR_LOAD_WAYBACK");
                t.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(this);
            }
        else loaded = false;
    }

    @Override
    public void onStopping() {
        while (disabling) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        if (loaded) {
            loaded = false;
            disabling = true;
            DelegatedWayback.onDisable();
            disabling = false;
        }
    }

}
