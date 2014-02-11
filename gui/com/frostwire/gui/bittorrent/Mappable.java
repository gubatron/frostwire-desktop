package com.frostwire.gui.bittorrent;

import java.util.Map;

public interface Mappable<K,V> {
    public Map<K,V> asMap();
}
