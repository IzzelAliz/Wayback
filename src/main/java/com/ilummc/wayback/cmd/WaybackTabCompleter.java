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
            case 1:
            {
                return Arrays.asList("conf","task","reload","debug","rollback");
            }
            case 2: {
                List<String> rollbackList = new ArrayList<>();
                if (args[0].equalsIgnoreCase("rollback"))
                    WaybackConf.getConf().getStorages().values().stream()
                            .flatMap(storage -> storage.listAvailable().stream())
                            .sorted(Comparator.reverseOrder()).forEach(time -> rollbackList.add(time.toString()));
                return rollbackList;
            }
        }
        return null;
    }
}
