package com.ilummc.wayback;

import com.google.gson.JsonObject;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.ilummc.wayback.util.Jsons.getJsonParser;

public final class WaybackUpdater {

    private static final String GITHUB_API = "https://api.github.com/repos/IzzelAliz/Wayback/releases/latest";
    private static volatile String updateMsg;


    static void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Wayback.instance(), () -> {
            try {
                JsonObject fetch = fetch(GITHUB_API);
                if (!Wayback.instance().getDescription().getVersion().equals(fetch.get("tag_name").getAsString())) {
                    setUpdateMsg(TLocale.asString("UPDATER", fetch.get("tag_name").getAsString(),
                            fetch.get("name").getAsString(), fetch.get("body").getAsString()));
                } else {
                    setUpdateMsg("");
                }
            } catch (IOException ignored) {
            }
        }, 10, 20 * 60 * 12);
    }

    synchronized public static boolean isOutdated() {
        return updateMsg != null && !updateMsg.equals("");
    }

    synchronized public static String getUpdateMsg() {
        return updateMsg;
    }

    private synchronized static void setUpdateMsg(String updateMsg) {
        WaybackUpdater.updateMsg = updateMsg;
    }

    private static JsonObject fetch(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection connection = ((HttpURLConnection) u.openConnection());
        connection.setRequestMethod("GET");
        return getJsonParser().parse(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
    }

}
