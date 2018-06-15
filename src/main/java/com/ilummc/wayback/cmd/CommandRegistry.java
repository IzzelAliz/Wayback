package com.ilummc.wayback.cmd;

import com.ilummc.tlib.resources.TLocale;
import com.ilummc.wayback.Wayback;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandRegistry implements CommandExecutor {

    @SuppressWarnings("unchecked")
    private static Map<String, Method> registries = new CaseInsensitiveMap();

    public static void register(Object object) {
        Arrays.stream(object.getClass().getDeclaredMethods()).filter(method -> method.isAnnotationPresent(Handler.class))
                .filter(method -> method.getParameterTypes().length == 2 &&
                        method.getParameterTypes()[0].equals(String[].class) &&
                        method.getParameterTypes()[1].equals(CommandSender.class))
                .forEach(method -> {
                    method.setAccessible(true);
                    registries.put(method.getAnnotation(Handler.class).value(), method);
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
            Method method = registries.get(args[0]);
            if (method == null) {
                TLocale.sendTo(sender, "COMMANDS.UNKNOWN_SUB_COMMAND");
            } else {
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
