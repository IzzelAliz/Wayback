package com.ilummc.wayback.cmd;

import com.ilummc.wayback.WaybackConf;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class WaybackTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1: {
                return Arrays.asList("conf", "task", "reload", "debug", "rollback");
            }
            case 2: {
                if (args[0].equalsIgnoreCase("rollback")) {
                    List<String> rollbackList = new ArrayList<>();
                    rollbackList.add("list");
                    WaybackConf.getConf().getStorages().values().stream()
                            .flatMap(storage -> storage.listAvailable().stream())
                            .sorted(Comparator.reverseOrder()).forEach(time -> rollbackList.add(time.toString()));
                    return rollbackList;
                }
                if (args[0].equalsIgnoreCase("conf"))
                    return Arrays.asList("decrypt", "encrypt", "setup");
                if (args[0].equalsIgnoreCase("task"))
                    return Arrays.asList("list");
            }
            default:
                return null;
        }
    }
}
