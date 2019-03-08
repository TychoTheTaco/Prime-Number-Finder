package com.tycho.app.primenumberfinder.utils;

import java.util.HashMap;
import java.util.Map;

public class OneToOneMap<K, V>{

    private final Map<K, V> keyToValueMap = new HashMap<>();
    private final Map<V, K> valueToKeyMap = new HashMap<>();

    public void put(final K key, final V value){
        //Ensure 1-1 relationship
        if (!keyToValueMap.containsKey(key) && !valueToKeyMap.containsKey(value)){
            keyToValueMap.put(key, value);
            valueToKeyMap.put(value, key);
        }
    }

    public V get(final K key){
        return keyToValueMap.get(key);
    }

    public K getKey(final V value){
        return valueToKeyMap.get(value);
    }

    public void remove(final K key){
        final V value = keyToValueMap.get(key);
        if (value != null){
            keyToValueMap.remove(key);
            valueToKeyMap.remove(value);
        }
    }
}
