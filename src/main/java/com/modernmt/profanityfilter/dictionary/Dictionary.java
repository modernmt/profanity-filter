package com.modernmt.profanityfilter.dictionary;

import com.modernmt.profanityfilter.corpus.UnixLineReader;
import com.modernmt.profanityfilter.corpus.UnixLineWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Dictionary implements Iterable<Profanity> {

    public class Matcher {

        private final Pattern regex;

        Matcher(Pattern regex) {
            this.regex = regex;
        }

        public Profanity find(String input) {
            String strProfanity = findString(input);
            return strProfanity == null ? null : Dictionary.this.profanities.get(strProfanity);

        }

        public boolean matches(String input) {
            return findString(input) != null;
        }

        private String findString(String input) {
            input = ' ' + Text.normalize(input) + ' ';
            java.util.regex.Matcher matcher = regex.matcher(input);
            return matcher.find() ? matcher.group(1) : null;
        }

    }

    public static Dictionary read(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(stream);
        }
    }

    public static Dictionary read(InputStream stream) throws IOException {
        HashSet<Profanity> profanities = new HashSet<>();

        UnixLineReader reader = new UnixLineReader(stream, StandardCharsets.UTF_8);
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#")) continue;

            String[] cols = line.split("\t");
            String text = Text.normalize(cols[0]);
            float score = cols.length > 1 ? Float.parseFloat(cols[1]) : 1.f;

            profanities.add(new Profanity(text, score));
        }

        return new Dictionary(profanities);
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

        for (Profanity profanity : entries) {
            writer.writeLine(profanity.toString());
        }

        writer.flush();
    }

    private final Map<String, Profanity> profanities;

    public Dictionary(Set<Profanity> profanities) {
        this.profanities = new HashMap<>(profanities.size());
        for (Profanity profanity : profanities)
            this.profanities.put(profanity.text(), profanity);
    }

    public Matcher matcher(float threshold) {
        StringBuilder regex = new StringBuilder(" (");

        for (Profanity profanity : profanities.values()) {
            if (profanity.score() >= threshold)
                regex.append(profanity.text()).append('|');
        }

        regex.setLength(regex.length() - 1);
        regex.append(") ");

        return new Matcher(Pattern.compile(regex.toString()));
    }

    @Override
    public Iterator<Profanity> iterator() {
        return profanities.values().iterator();
    }

}
