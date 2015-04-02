package com.ca.apm.swat.epaplugins.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonHelper {

    private static final Pattern unpad = Pattern.compile(AsmProperties.JSON_REGEX);

    /**
     * Unpad a padded JSON string.
     * @param jsonWithPadding JSON string with padding
     * @return JSON string without padding or <code>null</code>
     * if the padding pattern was not found
     */
    public static String unpadJson(String jsonWithPadding) {
        Matcher matched = unpad.matcher(jsonWithPadding);

        if (matched.find()) {
            return matched.group(1);
        }
        return null;
    }

}
