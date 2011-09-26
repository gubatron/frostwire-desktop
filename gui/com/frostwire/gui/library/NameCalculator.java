package com.frostwire.gui.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class NameCalculator {

    private static final Pattern splitRegex = Pattern.compile("[~_ #-]");

    private final List<String> names;

    public NameCalculator(List<String> names) {
        this.names = names;
    }

    public String getName() {
        Map<String, Pair> map = new HashMap<String, Pair>();
        List<Pair> list = new ArrayList<Pair>(names.size() * 4);

        for (String name : names) {
            String[] tokens = splitRegex.split(name);
            for (String t : tokens) {
                if (t.trim().length() == 0) {
                    continue;
                }
                Pair p = map.get(t);
                if (p != null) {
                    p.frecuency++;
                } else {
                    p = new Pair(t);
                    map.put(t, p);
                    list.add(p);
                }
            }
        }

        // stable sort
        Collections.sort(list, new Comparator<Pair>() {
            public int compare(Pair o1, Pair o2) {
                return -Integer.valueOf(o1.frecuency).compareTo(Integer.valueOf(o2.frecuency));
            }
        });

        return getName(list);
    }

    private String getName(List<Pair> list) {
        if (list.size() == 1) {
            return list.get(0).token;
        } else if (list.size() == 2) {
            return list.get(0).token + " " + list.get(1).token;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size() && i < 5; i++) {
                if (list.get(0).frecuency - list.get(i).frecuency > 1) {
                    break;
                } else {
                    sb.append(list.get(i).token);
                    sb.append(" ");
                }
            }
            return sb.toString().trim();
        }
    }

    private class Pair {
        public Pair(String token) {
            this.token = token;
            this.frecuency = 1;
        }

        String token;
        int frecuency;

        @Override
        public String toString() {
            return "(" + token + ":" + frecuency + ")";
        }
    }
}
