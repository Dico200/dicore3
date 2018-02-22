package io.dico.dicore.config.serializers;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SerializationUtil {
    
    private SerializationUtil() {
    
    }
    
    public static Map<String, Object> toMap(Object object, boolean empty) {
        if (object instanceof Map) {
            //noinspection unchecked
            return (Map<String, Object>) object;
        }
        if (object instanceof ConfigurationSection) {
            return ((ConfigurationSection) object).getValues(false);
        }
        return empty ? Collections.emptyMap() : null;
    }
    
    public static Map<String, Object> getMap(Map<String, Object> map, String key, boolean defaultEmpty) {
        Map<String, Object> rv = toMap(map.get(key), false);
        if (rv == null && defaultEmpty) rv = Collections.emptyMap();
        return rv;
    }
    
    public static int getInt(Map<String, Object> map, String key, int def) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(Objects.toString(val));
        } catch (Exception ex) {
            return def;
        }
    }
    
    public static float getFloat(Map<String, Object> map, String key, float def) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        try {
            return Float.parseFloat(Objects.toString(val));
        } catch (Exception ex) {
            return def;
        }
    }
    
    public static double getDouble(Map<String, Object> map, String key, double def) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        try {
            return Double.parseDouble(Objects.toString(val));
        } catch (Exception ex) {
            return def;
        }
    }
    
    public static boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        Object val = map.get(key);
        if (val == null) return def;
        return val == Boolean.TRUE || "true".equals(val);
    }
    
    public static Boolean getBoolean(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        return val == Boolean.TRUE || "true".equals(val);
    }
    
    public static String getString(Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        if (val == null) return def;
        return val.toString();
    }
    
    public static Map<String, Object> newSection(Map<String, Object> map, String key) {
        Map<String, Object> rv = new LinkedHashMap<>();
        map.put(key, rv);
        return rv;
    }
    
}
