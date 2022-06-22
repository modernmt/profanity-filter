package com.modernmt.profanityfilter.dictionary;

import java.util.regex.Pattern;

public class Text {

    private static final Pattern BOUNDARIES = Pattern.compile("\\W+");

    public static String normalize(String text) {
        text = text.toLowerCase();
        text = BOUNDARIES.matcher(text).replaceAll(" ");
        text = text.trim();

        return text;
    }
}
