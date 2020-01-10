package com.ilummc.wayback;

import com.ilummc.wayback.cmd.Handler;
import com.ilummc.wayback.schedules.DelayedSchedule;
import com.ilummc.wayback.schedules.PeriodSchedule;
import com.ilummc.wayback.schedules.WaybackSchedules;
import com.ilummc.wayback.util.Reference;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Optional;

import static com.ilummc.wayback.Wayback.getSchedules;

public class WaybackCommand {

    @Handler(value = "conf", descriptor = "COMMANDS.CONF_USAGE", permission = "wayback.conf")
    private static void conf(String[] arg, CommandSender sender) {
        if (arg.length == 0) throw new NullPointerException(TLocale.asString("COMMANDS.ILLEGAL_ARGUMENT"));
        else switch (arg[0]) {
            case "unencrypt":
            case "unenc":
                WaybackConf.unencrypt(sender);
                break;
            case "enc":
            case "encrypt":
                if (arg.length < 2) throw new NullPointerException(TLocale.asString("INPUT_PASSWORD"));
                else WaybackConf.encrypt(arg[1], sender);
                break;
            case "setup":

            default:
        }
    }

    public static void printRunning() {
        WaybackSchedules.instance().getRunning().forEach(schedule -> {
            TLocale.sendToConsole(
                    "TASKS.RUNNING_TASK_FORMAT", schedule.id(), schedule.name(),
                    String.valueOf(NumberFormat.getPercentInstance().format(schedule.progress())),
                    Duration.ofMillis(schedule.eta()).toString().substring(2).toLowerCase());
            Bukkit.getConsoleSender().sendMessage("    " + schedule.detail());
        });
    }

    @Handler(value = "task", descriptor = "COMMANDS.TASK_USAGE", permission = "wayback.task")
    private static void task(String[] arg, CommandSender sender) {
        if (arg.length == 0) throw new NullPointerException(TLocale.asString("COMMANDS.ILLEGAL_ARGUMENT"));
        else if ("list".equals(arg[0])) {
            TLocale.sendTo(sender, "TASKS.LIST", String.valueOf(WaybackConf.getConf().getPoolSize()));
            printRunning();
            WaybackSchedules.instance().getPending().stream()
                    .filter(task -> !task.isRunning()).forEach(task -> {
                if (task instanceof DelayedSchedule) TLocale.sendTo(sender, "TASKS.DELAYING_TASK_FORMAT",
                        task.id(), task.name(), ((DelayedSchedule) task).timeToRun());
                else if (task instanceof PeriodSchedule) {
                    if (((PeriodSchedule) task).getLastRun() != 0)
                        TLocale.sendTo(sender, "TASKS.PERIOD_TASK_FORMAT",
                                task.id(), task.name(), ((PeriodSchedule) task).timeToRun(), ((PeriodSchedule) task).lastRun());
                    else TLocale.sendTo(sender, "TASKS.PERIOD_TASK_FORMAT2",
                            task.id(), task.name(), ((PeriodSchedule) task).timeToRun());
                } else TLocale.sendTo(sender, "TASKS.PENDING_TASK_FORMAT", task.id(), task.name());
            });
        }
    }


    @Handler(value = "reload", descriptor = "COMMANDS.RELOAD", permission = "wayback.reload")
    private static void reload(String[] arg, CommandSender sender) {
        if (getSchedules().getRunning().size() > 0) {
            TLocale.sendTo(sender, "RELOAD_TASK_RUNNING");
            return;
        }
        if (Wayback.reload()) {
            TLocale.sendTo(sender, "RELOAD_SUCCESS");
        } else {
            TLocale.sendTo(sender, "RELOAD_FAILED");
        }
    }

    @Handler(value = "debug", descriptor = "COMMANDS.DEBUG", permission = "wayback.debug")
    private static void debug(String[] arg, CommandSender sender) {
        if (Wayback.logger().getLevel() == 3)
            Wayback.logger().setLevel(1);
        else Wayback.logger().setLevel(3);
        TLocale.sendTo(sender, "COMMANDS.DEBUG_SWITCH", String.valueOf(Wayback.logger().getLevel()));
    }

    @Handler(value = "rollback", descriptor = "COMMANDS.ROLLBACK_USAGE", permission = "wayback.rollback")
    private static void rollback(String[] arg, CommandSender sender) {
        if (arg.length == 0) TLocale.sendTo(sender, "ROLLBACK.HELP");
        else if ("list".equalsIgnoreCase(arg[0])) {
            if (arg.length >= 2)
                WaybackConf.getConf().getStorage(arg[1]).listAvailable().stream()
                        .sorted(Comparator.reverseOrder()).forEach(time -> sender.sendMessage(time.toString()));
            else WaybackConf.getConf().getStorages().values().stream()
                    .flatMap(storage -> storage.listAvailable().stream())
                    .sorted(Comparator.reverseOrder()).forEach(time -> sender.sendMessage(time.toString()));
        } else if ("confirm".equalsIgnoreCase(arg[0])) {
            System.setProperty("tlib.forceAsync", "true");

            TLocale.sendToConsole("ROLLBACK.PREPARE");
            Bukkit.getPluginManager().registerEvents(new RejectJoiningListener(), Wayback.instance());

            TLocale.sendToConsole("ROLLBACK.PREPARE_KICK_PLAYERS");
            Bukkit.getWorlds().stream().flatMap(world -> world.getPlayers().stream())
                    .forEach(player -> player.kickPlayer(TLocale.asString("ROLLBACK.KICK_ROLLBACK")));

            WaybackConf.getConf().getRollback().create(time.getValue()).schedule().addToQueue();
            while (true)
                try {
                    Thread.sleep(50);
                } catch (Throwable ignored) {
                }
        } else {
            String[] split = arg[0].split("\\.");
            LocalDateTime time;
            switch (split.length) {
                case 3:
                    time = LocalDateTime.of(parse(split[0]), parse(split[1]), parse(split[2]), 0, 0);
                    break;
                case 4:
                    time = LocalDateTime.of(parse(split[0]), parse(split[1]), parse(split[2]), parse(split[3]), 0);
                    break;
                case 5:
                    time = LocalDateTime.of(parse(split[0]), parse(split[1]), parse(split[2]), parse(split[3]), parse(split[4]));
                    break;
                default:
                    TLocale.sendTo(sender, "ROLLBACK.DATE_ERROR");
                    return;
            }
            WaybackConf.getConf().getStorages().values().stream()
                    .map(storage -> storage.findNearest(time))
                    .filter(Optional::isPresent).map(Optional::get)
                    .min(Comparator.comparingInt(o -> (int) Math.abs(time.toEpochSecond(ZoneOffset.MIN) - o.getTime().toEpochSecond(ZoneOffset.MIN))))
                    .ifPresent(breakpoint -> {
                        WaybackCommand.time.setValue(breakpoint.getTime());
                        TLocale.sendTo(sender, "COMMANDS.CONFIRM_BACKUP", breakpoint.getTime().toString());
                    });
        }
    }

    private static Reference<LocalDateTime> time = Reference.of(LocalDateTime.now());

    private static int parse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            return 0;
        }
    }

    private static final class RejectJoiningListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onConnect(PlayerLoginEvent event) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(TLocale.asString("ROLLBACK.KICK_ROLLBACK"));
        }

    }

}
