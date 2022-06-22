package com.modernmt.text.profanity;

import com.modernmt.text.profanity.dictionary.Dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfanityFilter {

    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "en", "it"
    );

    private static Dictionary loadDictionary(String language) {
        String resource = "dictionary." + language;

        InputStream stream = ProfanityFilter.class.getResourceAsStream(resource);
        if (stream == null) throw new RuntimeException("Internal resource not found: " + resource);

        try (stream) {
            return Dictionary.read(stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load internal resource: " + resource);
        }
    }

    private final Map<String, Dictionary.Matcher> matchers;

    public ProfanityFilter() {
        this(.3f);
    }

    public ProfanityFilter(float threshold) {
        matchers = new HashMap<>(SUPPORTED_LANGUAGES.size());
        for (String language : SUPPORTED_LANGUAGES) {
            Dictionary dictionary = loadDictionary(language);
            matchers.put(language, dictionary.matcher(threshold));
        }
    }

    public boolean test(String language, String text) {
        Dictionary.Matcher matcher = matchers.get(language);
        return matcher != null && matcher.matches(text);
    }

}
