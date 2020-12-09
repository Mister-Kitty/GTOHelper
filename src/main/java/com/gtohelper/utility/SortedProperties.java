package com.gtohelper.utility;

import java.util.*;

public class SortedProperties extends Properties {
    private static final long serialVersionUID = 1L;

    @Override
    public Set<Object> keySet() {
        return Collections.unmodifiableSet(new TreeSet<>(super.keySet()));
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        Set<Map.Entry<Object, Object>> set1 = super.entrySet();
        Set<Map.Entry<Object, Object>> set2 = new LinkedHashSet<>(set1.size());

        Iterator<Map.Entry<Object, Object>> iterator = set1.stream().sorted(Comparator.comparing(o -> o.getKey().toString())).iterator();

        while (iterator.hasNext())
            set2.add(iterator.next());

        return set2;
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }
}


