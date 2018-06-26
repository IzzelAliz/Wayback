package com.ilummc.wayback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ilummc.tlib.resources.TLocale;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

final class WaybackUpdater {

    public static final String GITHUB_API = "https://api.github.com/repos/IzzelAliz/Wayback/releases/latest";

    static void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Wayback.instance(), () -> {
            try {
                JsonObject fetch = fetch(GITHUB_API);
                if (!String.valueOf(Wayback.instance().getDescription().getVersion()).equals(fetch.get("tag_name").getAsString()))
                    TLocale.sendToConsole("UPDATER", fetch.get("tag_name").getAsString(),
                            fetch.get("name").getAsString(), fetch.get("body").getAsString());
            } catch (IOException ignored) {
            }
        }, 10, 20 * 60 * 12);
    }

    private static JsonObject fetch(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection connection = ((HttpURLConnection) u.openConnection());
        connection.setRequestMethod("GET");
        return new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
    }

}
