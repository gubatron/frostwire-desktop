package com.frostwire.torrent;

import java.util.Map;

public interface Mappable<K,V> {
    public Map<K,V> asMap();
}
