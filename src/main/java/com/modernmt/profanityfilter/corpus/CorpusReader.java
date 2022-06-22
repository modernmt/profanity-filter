package com.modernmt.profanityfilter.corpus;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CorpusReader implements Closeable {

    private final UnixLineReader sourceReader;
    private final UnixLineReader targetReader;

    public CorpusReader(Corpus corpus) throws IOException {
        boolean success = false;

        try {
            this.sourceReader = new UnixLineReader(new FileInputStream(corpus.source()), StandardCharsets.UTF_8);
            this.targetReader = new UnixLineReader(new FileInputStream(corpus.target()), StandardCharsets.UTF_8);
            success = true;
        } finally {
            if (!success)
                this.close();
        }
    }

    public TranslationUnit read() throws IOException {
        String source = sourceReader.readLine();
        String target = targetReader.readLine();

        if (source == null && target == null) {
            return null;
        } else if (source != null && target != null) {
            return new TranslationUnit(source, target);
        } else {
            throw new IOException("Invalid parallel corpus, unmatched line");
        }
    }

    @Override
    public void close() throws IOException {
        IOException ioe = null;

        try {
            if (this.sourceReader != null)
                this.sourceReader.close();
        } catch (IOException e) {
            ioe = e;
        }

        try {
            if (this.targetReader != null)
                this.targetReader.close();
        } catch (IOException e) {
            ioe = e;
        }

        if (ioe != null)
            throw ioe;
    }

}
