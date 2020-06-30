package dev.withajoint.rgxreplaceio;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class ReplaceWriter extends FilterWriter {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private char[] buffer;
    private BufferContentReplacer contentReplacer;
    private int nextChar;
    private int incompleteMatchStartIndex;

    public ReplaceWriter(Writer out, String regex, String replaceWith) {
        this(out, regex, replaceWith, DEFAULT_BUFFER_SIZE);
    }

    public ReplaceWriter(Writer out, String regex, String replaceWith, int bufferSize) {
        super(out);
        if (bufferSize <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        buffer = new char[bufferSize];
        contentReplacer = new BufferContentReplacer(regex, replaceWith);
        nextChar = 0;
        incompleteMatchStartIndex = -1;
    }

    @Override
    public void write(int c) throws IOException {
        if (nextChar >= buffer.length)
            writeOnUnderlyingStream();
        buffer[nextChar++] = (char) c;
    }

    private void writeOnUnderlyingStream() throws IOException {
        out.write(buffer, 0, nextChar);
        nextChar = 0;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        super.write(str, off, len);
    }

    @Override
    public void flush() throws IOException {
        replaceMatchingContent();
        writeOnUnderlyingStream();
        out.flush();
    }

    private void replaceMatchingContent() {
        buffer = contentReplacer.replaceMatchesIfAny(buffer, nextChar);
        nextChar = contentReplacer.getCharsAfterReplacement();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
