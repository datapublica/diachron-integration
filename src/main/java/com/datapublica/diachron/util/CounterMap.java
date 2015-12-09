package com.datapublica.diachron.util;

import java.util.HashMap;

/**
 * @author Jacques Belissent
 */
public class CounterMap<K> extends HashMap<K, Long> {

    public void add(K key, long l) {
        Long v = this.get(key);
        if (v == null) {
            v = 0L;
        }
        this.put(key, l + v);
    }
}
