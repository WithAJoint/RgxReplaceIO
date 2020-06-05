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
    private boolean bufferCheckedForReplacement;
    private int incompleteMatchStartIndex;
    private int charsInBuffer;
    private int nextChar;

    public ReplaceReader(Reader in, String regex, String replaceWith) {
        this(in, regex, replaceWith, DEFAULT_BUFFER_SIZE);
    }

    public ReplaceReader(Reader in, String regex, String replaceWith, int bufferSize) {
        super(in);
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        } else if (regex.isBlank())
            throw new IllegalArgumentException("Invalid regex");
        pattern = Pattern.compile(regex);
        this.replaceWith = replaceWith;
        buffer = new char[bufferSize];
        nextChar = charsInBuffer = 0;
        incompleteMatchStartIndex = -1;
        bufferCheckedForReplacement = false;
    }

    @Override
    public int read() throws IOException {
        if (nextChar >= charsInBuffer || nextChar == incompleteMatchStartIndex) {
            fillBuffer();
            if (charsInBuffer == 0)
                return -1;
        }
        return buffer[nextChar++];
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        int charsRead = 0;
        int maxCharsToRead;
        while (len > 0) {
            fillBuffer();
            maxCharsToRead = Math.min(len, buffer.length);
            if (charsInBuffer == 0 && charsRead == 0)
                return -1;
            else if (incompleteMatchStartIndex != -1 && nextChar + maxCharsToRead > incompleteMatchStartIndex)
                maxCharsToRead = incompleteMatchStartIndex - nextChar;
            else if (nextChar + maxCharsToRead > charsInBuffer)
                maxCharsToRead = len = charsInBuffer - nextChar;
            System.arraycopy(buffer, nextChar, cbuf, off + charsRead, maxCharsToRead);
            nextChar += maxCharsToRead;
            charsRead += maxCharsToRead;
            len -= maxCharsToRead;
        }
        return charsRead;
    }

    public String readLine() {
        return "";
    }

    private void fillBuffer() throws IOException {
        if (incompleteMatchStartIndex > 0) {
            reallocateBuffer(incompleteMatchStartIndex);
            incompleteMatchStartIndex = -1;
            bufferCheckedForReplacement = false;
        } else if (nextChar >= charsInBuffer) {
            charsInBuffer = 0;
            nextChar = 0;
        }
        int charsRead = 0;
        while (charsInBuffer < buffer.length && charsRead != -1) {
            charsRead = in.read(buffer, charsInBuffer, buffer.length - charsInBuffer);
            if (charsRead != -1) {
                charsInBuffer += charsRead;
                bufferCheckedForReplacement = false;
            }
            if (!bufferCheckedForReplacement)
                findAndReplace();
        }
    }

    private void reallocateBuffer(int startingPoint) throws IOException {
        int contentToReallocateLength = buffer.length - startingPoint;
        char[] tmpBuffer = new char[buffer.length];
        System.arraycopy(buffer, startingPoint, tmpBuffer, 0, contentToReallocateLength);
        buffer = tmpBuffer;
        charsInBuffer = contentToReallocateLength;
        nextChar = 0;
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
                if (replacedContent.length() > buffer.length) {
                    if (incompleteMatchStartIndex != -1)
                        incompleteMatchStartIndex += replacedContent.length() - buffer.length;
                    buffer = replacedContent.toCharArray();
                } else {
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
        bufferCheckedForReplacement = true;
    }

    @Override
    public long skip(long n) throws IOException {
        long skippedCharsCount = 0;
        long charsToSkip;
        while (skippedCharsCount < n) {
            charsToSkip = n - skippedCharsCount;
            fillBuffer();
            if (charsInBuffer == 0)
                break;
            else if (charsToSkip < charsInBuffer - nextChar) {
                skippedCharsCount += charsToSkip;
                nextChar += charsToSkip;
            } else {
                skippedCharsCount += charsInBuffer - nextChar;
                nextChar = charsInBuffer;
            }
        }
        return skippedCharsCount;
    }

    @Override
    public boolean ready() throws IOException {
        if (charsInBuffer == 0)
            fillBuffer();
        if (in.ready() && charsInBuffer != 0)
            return true;
        return false;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark() not supported");
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("reset() not supported");
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
