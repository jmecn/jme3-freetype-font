package io.github.jmecn.font.editor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class OrderedProperties extends Properties {
    private final LinkedHashSet<Object> keys = new LinkedHashSet<>();

    @Override
    public synchronized Object setProperty(String key, String value) {
        keys.add(key);
        return super.setProperty(key, value);
    }

    @Override
    public Set<String> stringPropertyNames() {
        return keys.stream().map(Object::toString).collect(Collectors.toSet());
    }

    @Override
    public Enumeration<Object> keys() {
        return Collections.enumeration(keys);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        Set<Map.Entry<Object, Object>> set = new LinkedHashSet<>();
        for (Object key : keys) {
            set.add(new AbstractMap.SimpleEntry<>(key, get(key)));
        }
        return set;
    }
}
