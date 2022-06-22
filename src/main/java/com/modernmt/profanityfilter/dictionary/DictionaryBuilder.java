package com.modernmt.profanityfilter.dictionary;

import com.modernmt.profanityfilter.corpus.Corpus;
import com.modernmt.profanityfilter.corpus.CorpusReader;
import com.modernmt.profanityfilter.corpus.TranslationUnit;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DictionaryBuilder {

    private final int threads;

    public DictionaryBuilder() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public DictionaryBuilder(int threads) {
        this.threads = threads;
    }

    public Dictionary build(List<Corpus> corpora, Dictionary input, Dictionary reference) throws IOException {
        FixedThreadsExecutor executor = new FixedThreadsExecutor(threads);

        Dictionary.Matcher sentenceMatcher = input.matcher(0.f);
        Dictionary.Matcher translationMatcher = reference.matcher(0.f);

        ConcurrentHashMap<Profanity, Counter> table = new ConcurrentHashMap<>();

        try {
            for (Corpus corpus : corpora) {
                try (CorpusReader reader = new CorpusReader(corpus)) {
                    TranslationUnit _tu;
                    while ((_tu = reader.read()) != null) {
                        TranslationUnit tu = _tu;

                        executor.submit(() -> {
                            Profanity profanity = sentenceMatcher.find(tu.sentence());
                            if (profanity != null) {
                                Counter counter = table.computeIfAbsent(profanity, Counter::new);
                                counter.frequency.incrementAndGet();

                                if (translationMatcher.matches(tu.translation()))
                                    counter.cooccurrences.incrementAndGet();
                            }
                        });
                    }
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1L, TimeUnit.DAYS)) {
                    executor.shutdownNow();
                    throw new RuntimeException("Timeout");
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }

        return createDictionary(table);
    }

    private Dictionary createDictionary(Map<Profanity, Counter> table) {
        Set<Profanity> profanities = new HashSet<>(table.size());

        for (Counter counter : table.values()) {
            String text = counter.profanity.text();
            double score = counter.cooccurrences.get() / ((double) counter.frequency.get());

            profanities.add(new Profanity(text, (float) score));
        }

        return new Dictionary(profanities);
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
        Dictionary input = Dictionary.read(inputDictionaryFile);
        Dictionary reference = Dictionary.read(refDictionaryFile);

        DictionaryBuilder builder = new DictionaryBuilder();
        Dictionary output = builder.build(corpora, input, reference);

        output.write(outputDictionaryFile);
    }
}
