package org.ijsberg.iglu.util.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class StringCache {
    private static int count = 0;
    private static Map<String, Integer> numberByString = new TreeMap<>();
    private static Map<Integer, String> stringByNumber = new TreeMap<>();

    private StringCache(){}

    public static int storeString(String string) {
        Integer number = numberByString.get(string);
        if(number == null) {
            number = count++;
            numberByString.put(string, number);
            stringByNumber.put(number, string);
        }
        return number;
    }

    public static String getString(int number) {
        return stringByNumber.get(number);
    }

    public static int getSize() {
        return numberByString.size();
    }
}
