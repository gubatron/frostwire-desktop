package com.frostwire.search.extractors.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class JavaFunctions {

    private JavaFunctions() {
    }

    public static boolean isdigit(String str) {
        if (str.length() == 0) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isalpha(String str) {
        if (str.length() == 0) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLetter(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static Object[] list(String str) {
        Object[] r = new Object[str.length()];

        for (int i = 0; i < str.length(); i++) {
            r[i] = str.charAt(i);
        }

        return r;
    }

    public static String join(Object[] arr) {
        StringBuilder sb = new StringBuilder();

        for (Object obj : arr) {
            sb.append(obj.toString());
        }

        return sb.toString();
    }

    public static Integer len(Object obj) {

        if (obj instanceof Object[]) {
            return ((Object[]) obj).length;
        }

        if (obj instanceof String) {
            return ((String) obj).length();
        }

        throw new IllegalArgumentException("Not supported type");
    }

    public static Object reverse(Object obj) {

        if (obj instanceof Object[]) {
            List<Object> list = new ArrayList<Object>();
            list.addAll(Arrays.asList((Object[]) obj));
            Collections.reverse(list);
            return list.toArray();
        }

        if (obj instanceof String) {
            return new StringBuilder((String) obj).reverse().toString();
        }

        throw new IllegalArgumentException("Not supported type");
    }

    public static Object splice(Object obj, int fromIndex) {

        if (obj instanceof Object[]) {
            return Arrays.asList((Object[]) obj).subList(fromIndex, ((Object[]) obj).length).toArray();
        }

        if (obj instanceof String) {
            return ((String) obj).substring(fromIndex);
        }

        throw new IllegalArgumentException("Not supported type");
    }
}
