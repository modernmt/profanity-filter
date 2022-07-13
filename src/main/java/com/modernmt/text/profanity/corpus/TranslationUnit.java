package com.modernmt.text.profanity.corpus;

public class TranslationUnit {

    private final String sentence;
    private final String translation;

    public TranslationUnit(String sentence, String translation) {
        this.sentence = sentence;
        this.translation = translation;
    }

    public String sentence() {
        return sentence;
    }

    public String translation() {
        return translation;
    }

}
