package com.modernmt.text.profanity.corpus;

public record TranslationUnit(String sentence, String translation) {
    @Override
    public String toString() {
        return sentence + '\t' + "###" + '\t' + translation;
    }
}
