package com.ilummc.wayback.cmd;

import com.ilummc.wayback.Wayback;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandRegistry implements CommandExecutor {

    private static Map<Handler, Method> registries = new HashMap<>();

    public static void register(Object object) {
        Arrays.stream(object.getClass().getDeclaredMethods()).filter(method -> method.isAnnotationPresent(Handler.class))
                .filter(method -> method.getParameterTypes().length == 2 &&
                        method.getParameterTypes()[0].equals(String[].class) &&
                        method.getParameterTypes()[1].equals(CommandSender.class))
                .forEach(method -> {
                    method.setAccessible(true);
                    registries.put(method.getAnnotation(Handler.class), method);
                });
    }

    public static void init() {
        Wayback.instance().getCommand("wayback").setExecutor(new CommandRegistry());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
            registries.values().stream().map(method -> method.getAnnotation(Handler.class))
                    .forEach(handler -> TLocale.sendTo(sender, "USAGE", "/wayback " + handler.value(), TLocale.asString(handler.descriptor())));
        else {
            Map.Entry<Handler, Method> entry = registries.entrySet().stream().filter(en -> en.getKey().value().equals(args[0].toLowerCase()))
                    .findAny().orElse(null);
            if (entry == null || entry.getValue() == null) {
                TLocale.sendTo(sender, "COMMANDS.UNKNOWN_SUB_COMMAND");
            } else {
                if (!sender.hasPermission(entry.getKey().permission()) && sender != Bukkit.getConsoleSender()) {
                    TLocale.sendTo(sender, "COMMANDS.NO_PERMISSION");
                    return true;
                }
                Method method = entry.getValue();
                String[] newArg = new String[args.length - 1];
                System.arraycopy(args, 1, newArg, 0, args.length - 1);
                try {
                    method.invoke(null, newArg, sender);
                } catch (IllegalAccessException ignored) {
                } catch (InvocationTargetException e) {
                    TLocale.sendTo(sender, "COMMANDS.ERROR_EXECUTE", e.getCause().getMessage());
                }
            }
        }
        return true;
    }
}
