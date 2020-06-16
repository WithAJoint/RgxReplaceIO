package dev.withajoint.rgxreplaceio;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

public class ReplaceWriter  extends FilterWriter {

    private String replaceWith;

    public ReplaceWriter(Writer out, String regex, String replaceWith) {
        super(out);
    }

    @Override
    public void write(int c) throws IOException {
        super.write(c);
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
        super.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
