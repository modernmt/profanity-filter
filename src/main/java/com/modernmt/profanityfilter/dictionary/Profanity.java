package com.modernmt.profanityfilter.dictionary;

import java.util.Objects;

public record Profanity(String text, float score) implements Comparable<Profanity> {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profanity profanity = (Profanity) o;
        return text.equals(profanity.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return text + '\t' + score;
    }

    @Override
    public int compareTo(Profanity o) {
        return text.compareTo(o.text);
    }
}
