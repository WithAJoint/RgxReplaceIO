package dev.withajoint.rgxreplaceio;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceReader extends FilterReader {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private char[] buffer;
    private final Pattern pattern;
    private final String replaceWith;
    private int incompleteRegexMatch;
    private int charsInBuffer;
    private int nextChar;
    private int markedChar;
    private int readAheadLimit;

    public ReplaceReader(Reader in, String regex, String replaceWith) {
        this(in, regex, replaceWith, DEFAULT_BUFFER_SIZE);
    }

    public ReplaceReader(Reader in, String regex, String replaceWith, int bufferSize) {
        super(in);
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        pattern = Pattern.compile(regex);
        this.replaceWith = replaceWith;
        buffer = new char[bufferSize];
        nextChar = charsInBuffer = 0;
        incompleteRegexMatch = -1;
    }

    @Override
    public int read() throws IOException {
        if (nextChar >= charsInBuffer) {
            fillBuffer();
            if (nextChar >= charsInBuffer)
                return -1;
        } else if (nextChar == incompleteRegexMatch)
            fillBuffer();
        return buffer[nextChar++];
    }


    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return super.read(cbuf, off, len);
    }

    public String readLine() {
        return "";
    }

    private void fillBuffer() throws IOException {
        if (incompleteRegexMatch > 0) {
            reallocateBuffer();
            charsInBuffer += in.read(buffer, charsInBuffer, buffer.length - charsInBuffer);
            incompleteRegexMatch = -1;
        } else
            charsInBuffer = in.read(buffer);
        if (charsInBuffer > 0)
            findAndReplace();
        nextChar = 0;
    }

    private void reallocateBuffer() throws IOException {
        charsInBuffer = buffer.length - incompleteRegexMatch;
        char[] tmpBuffer = new char[buffer.length];
        System.arraycopy(buffer, incompleteRegexMatch, tmpBuffer, 0, charsInBuffer);
        buffer = tmpBuffer;
    }

    private void findAndReplace() {
        String bufferContent = String.valueOf(buffer);
        Matcher matcher = pattern.matcher(bufferContent);
        if (matcher.find()) {
            if (matcher.end() == buffer.length && matcher.start() == 0)
                throw new IllegalStateException("Regex match too broad, increase buffer size");
            String replacedContent = matcher.replaceAll(matchResult -> {
                if (matchResult.end() == buffer.length) {
                    incompleteRegexMatch = matcher.start();
                    return matchResult.group();
                }
                charsInBuffer += replaceWith.length() - matchResult.group().length();
                return replaceWith;
            });
            if (!replacedContent.contentEquals(bufferContent)) {
                if (replacedContent.length() > buffer.length)
                    buffer = replacedContent.toCharArray();
                else {
                    /*
                     * matcher.replaceAll() for some reasons returns a string long 8191 chars
                     * instead of 8192 (default buffer size used), these instructions prevent
                     * the buffer from shrinking each time.
                     */
                    char[] tmpBuffer = new char[buffer.length];
                    char[] newContent = replacedContent.toCharArray();
                    System.arraycopy(newContent, 0, tmpBuffer, 0, newContent.length);
                    buffer = tmpBuffer;
                }
            }
        }
    }

    @Override
    public long skip(long n) throws IOException {
        return super.skip(n);
    }

    @Override
    public boolean ready() throws IOException {
        return super.ready();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        super.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
