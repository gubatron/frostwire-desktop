package com.frostwire.util;

public class Condition {

    /**
     * Useful to shorten long "or" boolean expressions.
     * @param needle
     * @param args
     * @return true if needle is in any of the args.
     */
    public static <T> boolean in(T needle, T... args) {
        boolean in = false;
        for (T t : args) {
            if (t.equals(needle)) {
                in = true;
                break;
            }
        }
        return in;
    }

}