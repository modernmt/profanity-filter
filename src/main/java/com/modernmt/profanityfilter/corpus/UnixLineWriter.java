package com.modernmt.profanityfilter.corpus;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by davide on 04/07/16.
 */
public class UnixLineWriter implements Closeable {

    protected final Writer writer;

    public UnixLineWriter(File file, Charset charset) throws FileNotFoundException {
        this(new FileOutputStream(file), charset);
    }

    public UnixLineWriter(OutputStream stream, Charset charset) {
        this(new OutputStreamWriter(stream, charset));
    }

    public UnixLineWriter(Writer writer) {
        this.writer = writer;
    }

    public void flush() throws IOException {
        this.writer.flush();
    }

    public void writeLine(String line) throws IOException {
        this.writer.write(line.replace('\n', ' '));
        this.writer.write('\n');
    }

    @Override
    public void close() throws IOException {
        this.writer.flush();
        this.writer.close();
    }
}
