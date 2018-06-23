package com.ilummc.wayback;

import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.annotations.Logger;
import com.ilummc.tlib.logger.TLogger;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.wayback.schedules.WaybackSchedules;
import me.skymc.taboolib.TabooLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Dependency(type = Dependency.Type.LIBRARY, maven = "it.sauronsoftware:ftp4j:1.7.2", mavenRepo = "https://bkm016.github.io/TabooLib/repo")
//@Dependency(type = Dependency.Type.LIBRARY, maven = "net.sf.sevenzipjbinding:sevenzipjbinding:9.20-2.00beta")
//@Dependency(type = Dependency.Type.LIBRARY, maven = "net.sf.sevenzipjbinding:sevenzipjbinding-all-platforms:9.20-2.00beta")
@Dependency(type = Dependency.Type.LIBRARY, maven = "commons-collections:commons-collections:3.2.2")
@Dependency(type = Dependency.Type.LIBRARY, maven = "net.lingala.zip4j:zip4j:1.3.2")
public final class Wayback extends JavaPlugin {

    @Logger("[{0}][{1}§f] {2}")
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

    @Override
    public void onLoad() {
        instance = this;
        if (Bukkit.getPluginManager().getPlugin("TabooLib") == null || TabooLib.getPluginVersion() < 4.09) {
            Bukkit.getConsoleSender().sendMessage("§cThis plugin needs TabooLib 4.09+ as dependency !!!!!!");
            Bukkit.getConsoleSender().sendMessage("§c此插件需要 TabooLib 4.09+ 作为依赖 !!!!!!");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            } finally {
                loaded = true;
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
    }

    @Override
    public void onEnable() {
        if (!loaded)
            try {
                DelegatedWayback.onEnable(this);
                loaded = true;
            } catch (Throwable t) {
                TLocale.Logger.fatal("ERR_LOAD_WAYBACK");
                t.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(this);
            }
        else loaded = false;
    }

    @Override
    public void onDisable() {
        while (disabling) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        if (loaded) {
            loaded = false;
            disabling = true;
            DelegatedWayback.onDisable(this);
            disabling = false;
        }
    }

}
