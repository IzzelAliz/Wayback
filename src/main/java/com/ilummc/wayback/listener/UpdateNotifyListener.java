package com.ilummc.wayback.listener;

import com.ilummc.wayback.Wayback;
import com.ilummc.wayback.WaybackUpdater;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotifyListener implements Listener {

    @EventHandler
    public void onAdminLogin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(Wayback.instance(), () -> {
            if (WaybackUpdater.isOutdated() && (event.getPlayer().isOp() || event.getPlayer().hasPermission("wayback.updateNotify"))) {
                event.getPlayer().sendMessage(WaybackUpdater.getUpdateMsg());
            }
        });
    }
}
