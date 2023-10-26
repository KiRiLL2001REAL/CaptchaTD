package cw.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class JsonUtil {
    private static final Gson GSON;
    static {
        GsonBuilder GB = new GsonBuilder().setPrettyPrinting();
        GSON = GB.create();
    }

    public static String serialize(Object object) {
        return GSON.toJson(object);
    }

    public static Object deserialize(String json, Class<?> clazz) throws JsonSyntaxException {
        return GSON.fromJson(json, clazz);
    }
}