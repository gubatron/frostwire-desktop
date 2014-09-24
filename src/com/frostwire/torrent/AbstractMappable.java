package com.frostwire.torrent;

import java.util.Map;

public abstract class AbstractMappable<K, V> implements Mappable<K, V>{

    @Override
    public Map<K, V> asMap() {
        // TODO Auto-generated method stub
        return null;
    }

    public static String getStringFromEncodedMap(String key, Map<String, Object> map) {
        String result = null;
        if (map.get(key) != null && map.get(key) instanceof byte[]) {
            result = new String((byte[]) map.get(key));
        } else if (map.get(key) != null && map.get(key) instanceof String && !((String) map.get(key)).isEmpty()) {
            result = (String) map.get(key);
        }
        return result;
    }
}
