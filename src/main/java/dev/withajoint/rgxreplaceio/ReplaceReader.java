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
    private int incompleteMatchStartIndex;
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
        incompleteMatchStartIndex = -1;
    }

    @Override
    public int read() throws IOException {
        if (nextChar >= charsInBuffer || nextChar == incompleteMatchStartIndex) {
            fillBuffer();
            if (nextChar >= charsInBuffer)
                return -1;
        }
        return buffer[nextChar++];
    }


    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (nextChar >= charsInBuffer) {
            fillBuffer();
        }
        //in progress
        return 0;
    }

    public String readLine() {
        return "";
    }

    private void fillBuffer() throws IOException {
        if (incompleteMatchStartIndex > 0) {
            reallocateBuffer();
            int charRead = in.read(buffer, charsInBuffer, buffer.length - charsInBuffer);
            if (charRead != -1)
                charsInBuffer += charRead;
            incompleteMatchStartIndex = -1;
        } else
            charsInBuffer = in.read(buffer);
        nextChar = 0;
        if (charsInBuffer > 0)
            findAndReplace();
    }

    private void reallocateBuffer() throws IOException {
        int incompleteMatchLength = buffer.length - incompleteMatchStartIndex;
        char[] tmpBuffer = new char[buffer.length];
        System.arraycopy(buffer, incompleteMatchStartIndex, tmpBuffer, 0, incompleteMatchLength);
        buffer = tmpBuffer;
        charsInBuffer = incompleteMatchLength;
    }

    private void findAndReplace() {
        String bufferContent = String.valueOf(buffer);
        Matcher matcher = pattern.matcher(bufferContent);
        if (matcher.find()) {
            if (matcher.end() == buffer.length && matcher.start() == 0)
                throw new IllegalStateException("Regex match too broad, increase buffer size");
            String replacedContent = matcher.replaceAll(matchResult -> {
                if (matchResult.end() == buffer.length) {
                    incompleteMatchStartIndex = matcher.start();
                    return matchResult.group();
                }
                charsInBuffer += replaceWith.length() - matchResult.group().length();
                return replaceWith;
            });
            if (!replacedContent.contentEquals(bufferContent) && charsInBuffer > 0) {
                if (replacedContent.length() > buffer.length)
                    buffer = replacedContent.toCharArray();
                else {
                    /*
                     * For some reasons matcher.replaceAll() returns a string long 8191 chars
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
