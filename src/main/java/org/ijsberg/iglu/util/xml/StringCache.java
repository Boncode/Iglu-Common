package org.ijsberg.iglu.util.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public abstract class StringCache {
    //private static int count = 0;
    private static Map<String, Integer> numberByString = new HashMap<>();
    //private static Map<Integer, String> stringByNumber = new HashMap<>();

    private static List<String> strings = new ArrayList();

    static {
        strings.add(null);
    }

/*    private static final List<String> sortedStrings = new ArrayList<String>() {
                public boolean add(String string) {
                    int index = Collections.binarySearch(this, string);
                    if (index < 0) index = ~index;
                    super.add(index, string);
                    return true;
                }
            };
*/

    private StringCache(){}

/*
    public static String getCachedString(String string) {
        int index = Collections.binarySearch(sortedStrings, string);
        if(index != -1) {
            return sortedStrings.get(index);
        } else {
            sortedStrings.add(string);
        }
        return string;
    }
*/
    public static int storeString(String string) {

        synchronized (numberByString) {
           // long start = System.currentTimeMillis();
            //strings.indexOf(string);
           // System.out.println(System.currentTimeMillis() - start);
            Integer number = numberByString.get(string);
            if (number == null) {
                number = strings.size();
                numberByString.put(string, number);
                //stringByNumber.put(number, string);
                strings.add(string);
            }
            return number;
        }
    }

    public static int getStringIndex(String string) {

        synchronized (numberByString) {
            // long start = System.currentTimeMillis();
            //strings.indexOf(string);
            // System.out.println(System.currentTimeMillis() - start);
            Integer index = numberByString.get(string);
            if(index == null) {
                return -1;
            } else {
                return index;
            }
        }
    }

    public static String getString(int number) {
        synchronized (numberByString) {
            return strings.get(number);
        }
    }

    public static int getSize() {
        return numberByString.size();
    }

    public static final void clear() {
        synchronized (numberByString) {
            numberByString.clear();
            strings.clear();
            strings.add(null);
        }
    }




    public static void main(String[] args) {
        try {
            // Encode a String into bytes
            String inputString = "blahblahblah@☺koklkadsblablabla";
            byte[] input = inputString.getBytes("UTF-8");

            System.out.println(input.length);

            // Compress the bytes
            byte[] output = new byte[100];
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            compresser.end();

            System.out.println(compressedDataLength);

            // Decompress the bytes
            Inflater decompresser = new Inflater();
            decompresser.setInput(output, 0, compressedDataLength);
            byte[] result = new byte[100];
            int resultLength = decompresser.inflate(result);
            decompresser.end();

            // Decode the bytes into a String
            String outputString = new String(result, 0, resultLength, "UTF-8");
        } catch(java.io.UnsupportedEncodingException ex) {
            // handle
        } catch (java.util.zip.DataFormatException ex) {
            // handle
        }
    }
}
