package com.ilummc.wayback;

import com.ilummc.tlib.resources.TLocale;
import com.ilummc.wayback.cmd.Handler;
import com.ilummc.wayback.schedules.DelayedSchedule;
import com.ilummc.wayback.schedules.PeriodSchedule;
import com.ilummc.wayback.schedules.WaybackSchedules;
import org.bukkit.command.CommandSender;

import java.text.NumberFormat;
import java.time.Duration;

public class WaybackCommand {

    @Handler(value = "conf", descriptor = "COMMANDS.CONF_USAGE")
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
        WaybackSchedules.instance().getRunning().forEach(schedule -> TLocale.sendToConsole(
                "TASKS.RUNNING_TASK_FORMAT", schedule.id(), schedule.name(),
                String.valueOf(NumberFormat.getPercentInstance().format(schedule.progress())),
                Duration.ofMillis(schedule.eta()).toString().substring(2).toLowerCase()));
    }

    @Handler(value = "task", descriptor = "COMMANDS.TASK_USAGE")
    private static void task(String[] arg, CommandSender sender) {
        if (arg.length == 0) throw new NullPointerException(TLocale.asString("COMMANDS.ILLEGAL_ARGUMENT"));
        else switch (arg[0]) {
            case "list":
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
                break;
            default:
        }
    }

    @Handler(value = "debug", descriptor = "COMMANDS.DEBUG")
    private static void debug(String[] arg, CommandSender sender) {
        if (Wayback.logger().getLevel() == 3)
            Wayback.logger().setLevel(1);
        else Wayback.logger().setLevel(3);
        TLocale.sendTo(sender, "COMMANDS.DEBUG_SWITCH", String.valueOf(Wayback.logger().getLevel()));
    }
}
