package com.modernmt.text.profanity.corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Corpus {

    private final File source;
    private final File target;

    public Corpus(File source, File target) {
        this.source = source;
        this.target = target;
    }

    public File source() {
        return source;
    }

    public File target() {
        return target;
    }

    private static File fileWithExtension(File file, String extension) {
        String name = file.getName();
        int ld = name.lastIndexOf('.');
        if (ld >= 0)
            name = name.substring(0, ld);

        return new File(file.getParentFile(), name + "." + extension);
    }

    public static List<Corpus> list(String source, String target, File folder) throws FileNotFoundException {
        if (!folder.isDirectory())
            throw new FileNotFoundException(folder.toString());

        File[] sources = folder.listFiles((dir, name) -> name.endsWith("." + source));
        if (sources == null)
            return Collections.emptyList();

        ArrayList<Corpus> corpora = new ArrayList<>(sources.length);
        for (File sourceFile : sources) {
            File targetFile = fileWithExtension(sourceFile, target);
            if (targetFile.isFile())
                corpora.add(new Corpus(sourceFile, targetFile));
        }
        return corpora;
    }
}
