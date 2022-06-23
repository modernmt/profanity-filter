package com.modernmt.text.profanity.dictionary;

import java.util.Objects;

public record Profanity(String text, float score, int profanity_freq, int total_freq) implements Comparable<Profanity> {

    public Profanity(String text, float score) {
        this(text, score, 0, 0);
    }

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
        return text + '\t' + score + '\t' + profanity_freq + '\t' + total_freq;
    }

    @Override
    public int compareTo(Profanity o) {
        return Float.compare(score, o.score);
    }
}
