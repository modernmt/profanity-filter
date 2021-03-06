package com.modernmt.text.profanity.dictionary;

import com.modernmt.text.profanity.corpus.Corpus;
import com.modernmt.text.profanity.corpus.CorpusReader;
import com.modernmt.text.profanity.corpus.TranslationUnit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DictionaryBuilder {

    private final String language;
    private final int threads;

    public DictionaryBuilder(String language) {
        this(language, Runtime.getRuntime().availableProcessors());
    }

    public DictionaryBuilder(String language, int threads) {
        this.language = language;
        this.threads = threads;
    }

    public Dictionary build(List<Corpus> corpora, Dictionary input, Dictionary reference) throws IOException, InterruptedException {
        FixedThreadsExecutor executor = new FixedThreadsExecutor(threads);

        Dictionary.Matcher sentenceMatcher = input.matcher(0.f);
        Dictionary.Matcher translationMatcher = reference.matcher(0.f);

        Map<Profanity, Counter> table = new HashMap<>();
        for (Profanity profanity : input)
            table.put(profanity, new Counter(profanity));

        try {
            for (Corpus corpus : corpora) {
                try (CorpusReader reader = new CorpusReader(corpus)) {
                    TranslationUnit _tu;
                    while ((_tu = reader.read()) != null) {
                        TranslationUnit tu = _tu;

                        executor.submit(() -> {
                            Profanity profanity = sentenceMatcher.find(tu.sentence());
                            if (profanity != null) {
                                Counter counter = table.get(profanity);
                                counter.frequency.incrementAndGet();

                                if (translationMatcher.matches(tu.translation()))
                                    counter.cooccurrences.incrementAndGet();
                            }
                        });
                    }
                }
            }

            // wait for completion
            executor.shutdown();

            if (!executor.awaitTermination(1L, TimeUnit.DAYS))
                throw new InterruptedException("Timeout");
        } finally {
            executor.shutdownNow();
        }

        return createDictionary(table);
    }

    private Dictionary createDictionary(Map<Profanity, Counter> table) {
        Set<Profanity> profanities = new HashSet<>(table.size());

        for (Counter counter : table.values()) {
            String text = counter.profanity.text();
            int totalFreq = (int) counter.frequency.get();
            int profanityFreq = (int) counter.cooccurrences.get();
            double score = totalFreq > 0 ? ((float) profanityFreq / totalFreq) : 0.;

            profanities.add(new Profanity(text, (float) score));
        }

        return new Dictionary(language, profanities);
    }

    private static class Counter {

        public final Profanity profanity;
        public AtomicLong frequency = new AtomicLong(0L);
        public AtomicLong cooccurrences = new AtomicLong(0L);

        public Counter(Profanity profanity) {
            this.profanity = profanity;
        }
    }

    private static class FixedThreadsExecutor {

        private final ExecutorService executor;
        private final Semaphore permits;

        public FixedThreadsExecutor(int threads) {
            executor = Executors.newFixedThreadPool(threads);
            permits = new Semaphore(threads * 4);
        }

        public void submit(Runnable task) {
            try {
                permits.acquire();
            } catch (InterruptedException e) {
                throw new RejectedExecutionException(e);
            }

            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    permits.release();
                }
            });
        }

        public void shutdown() {
            executor.shutdown();
        }

        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return executor.awaitTermination(timeout, unit);
        }

        public void shutdownNow() {
            executor.shutdownNow();
        }
    }

    private static String extension(File file) {
        String name = file.getName();
        int idx = name.lastIndexOf('.');
        return idx < 0 ? null : name.substring(idx + 1);
    }

    public static void main(String[] args) throws Throwable {
        if (args.length != 4)
            throw new IllegalArgumentException("Usage: <main> CORPORA_PATH INPUT_DICT REF_DICT OUTPUT_DICT");

        File folder = new File(args[0]);
        File inputDictionaryFile = new File(args[1]);
        File refDictionaryFile = new File(args[2]);
        File outputDictionaryFile = new File(args[3]);

        String source = extension(inputDictionaryFile);
        String target = extension(refDictionaryFile);

        List<Corpus> corpora = Corpus.list(source, target, folder);
        Dictionary input = Dictionary.read(source, inputDictionaryFile);
        Dictionary reference = Dictionary.read(target, refDictionaryFile);

        DictionaryBuilder builder = new DictionaryBuilder(source);
        Dictionary output = builder.build(corpora, input, reference);

        output.write(outputDictionaryFile);
    }
}
