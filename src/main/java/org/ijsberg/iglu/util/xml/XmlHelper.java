package org.ijsberg.iglu.util.xml;

import org.ijsberg.iglu.util.misc.StringSupport;

public class XmlHelper {

    public static final String encodeAttributeValue(String value) {
        return StringSupport.replaceAll(value, new String[]{"&","<",">","\""}, new String[]{"&amp;","&lt;","&gt;","&quot;"});
    }
}
