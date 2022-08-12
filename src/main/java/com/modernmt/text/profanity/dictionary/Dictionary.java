package com.modernmt.text.profanity.dictionary;


import com.modernmt.text.profanity.corpus.UnixLineReader;
import com.modernmt.text.profanity.corpus.UnixLineWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Dictionary implements Iterable<Profanity> {

    public interface Matcher {

        Profanity find(String input);

        boolean matches(String input);

    }

    private static class FalseMatcher implements Matcher {

        public static final FalseMatcher INSTANCE = new FalseMatcher();

        @Override
        public Profanity find(String input) {
            return null;
        }

        @Override
        public boolean matches(String input) {
            return false;
        }

    }

    private class RegexMatcher implements Matcher {

        private final Pattern regex;

        RegexMatcher(Pattern regex) {
            this.regex = regex;
        }

        @Override
        public Profanity find(String input) {
            String strProfanity = findString(input);
            return strProfanity == null ? null : Dictionary.this.profanities.get(strProfanity);

        }

        @Override
        public boolean matches(String input) {
            return findString(input) != null;
        }

        private String findString(String input) {
            input = ' ' + Text.normalize(input) + ' ';
            java.util.regex.Matcher matcher = regex.matcher(input);
            return matcher.find() ? matcher.group(1) : null;
        }

    }

    public static Dictionary read(String language, File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(language, stream);
        }
    }

    public static Dictionary read(String language, InputStream stream) throws IOException {
        HashSet<Profanity> profanities = new HashSet<>();

        UnixLineReader reader = new UnixLineReader(stream, StandardCharsets.UTF_8);
        String line;
        while ((line = reader.readLine()) != null) {
            int idx = line.indexOf('#');
            if (idx >= 0)
                line = line.substring(0, idx);
            line = line.trim();

            if (line.isEmpty()) continue;

            String[] cols = line.split("\t");
            String text = Text.normalize(cols[0]);
            if (text.isEmpty())
                throw new IOException("Invalid value at line: \"" + line + "\"");
            float score = cols.length > 1 ? Float.parseFloat(cols[1]) : 1.f;

            profanities.add(new Profanity(text, score));
        }

        return new Dictionary(language, profanities);
    }

    public void write(File file) throws IOException {
        try (OutputStream stream = new FileOutputStream(file)) {
            write(stream);
        }
    }

    public void write(OutputStream stream) throws IOException {
        UnixLineWriter writer = new UnixLineWriter(stream, StandardCharsets.UTF_8);

        List<Profanity> entries = new ArrayList<>(profanities.values());
        Collections.sort(entries);
        Collections.reverse(entries);

        for (Profanity profanity : entries) {
            writer.writeLine(profanity.toString());
        }

        writer.flush();
    }

    private final Map<String, Profanity> profanities;
    private final boolean isSpaceSeparated;

    public Dictionary(String language, Set<Profanity> profanities) {
        this.profanities = new HashMap<>(profanities.size());
        for (Profanity profanity : profanities)
            this.profanities.put(profanity.text(), profanity);
        this.isSpaceSeparated = !("zh".equals(language) || "ja".equals(language) || "th".equals(language));
    }

    public Matcher matcher(float threshold) {
        StringBuilder regex = new StringBuilder();
        if (isSpaceSeparated) regex.append(' ');
        regex.append('(');

        boolean profanityFound = false;
        for (Profanity profanity : profanities.values()) {
            if (profanity.score() >= threshold) {
                regex.append(profanity.text()).append('|');
                profanityFound = true;
            }
        }

        if (!profanityFound) return FalseMatcher.INSTANCE;

        regex.setLength(regex.length() - 1);
        regex.append(')');
        if (isSpaceSeparated) regex.append(' ');

        return new RegexMatcher(Pattern.compile(regex.toString()));
    }

    @Override
    public Iterator<Profanity> iterator() {
        return profanities.values().iterator();
    }

}
