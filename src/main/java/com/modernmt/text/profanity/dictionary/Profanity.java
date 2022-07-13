package com.modernmt.text.profanity.dictionary;

import java.util.Objects;

public class Profanity implements Comparable<Profanity> {

    private final String text;
    private final float score;

    public Profanity(String text, float score) {
        this.text = text;
        this.score = score;
    }

    public String text() {
        return text;
    }

    public float score() {
        return score;
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
        return text + '\t' + score;
    }

    @Override
    public int compareTo(Profanity o) {
        return Float.compare(score, o.score);
    }
}
