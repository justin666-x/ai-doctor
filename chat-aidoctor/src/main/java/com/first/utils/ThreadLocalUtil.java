package com.first.utils;

import java.util.Map;

@SuppressWarnings("all")
public class ThreadLocalUtil {

    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

    public static Integer getUserId() {
        Map<String, Object> map = get();
        if (map == null) return null;
        Object id = map.get("id");
        if (id instanceof Number) return ((Number) id).intValue();
        return null;
    }

    public static String getUsername() {
        Map<String, Object> map = get();
        if (map == null) return null;
        return (String) map.get("username");
    }
}
