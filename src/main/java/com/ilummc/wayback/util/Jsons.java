package com.ilummc.wayback.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Map;

public class Jsons {

    private static Gson gson = new Gson();

    public static <T> T mapTo(Map<String, Object> map, Class<T> clazz) {
        return gson.fromJson(gson.toJsonTree(map), clazz);
    }

    public static <T> T mapTo(JsonObject object, Class<T> clazz) {
        return gson.fromJson(object, clazz);
    }

}
