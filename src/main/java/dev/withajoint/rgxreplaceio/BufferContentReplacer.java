package dev.withajoint.rgxreplaceio;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BufferContentReplacer {

    private String bufferContent;
    private int bufferSize;
    private Matcher matcher;
    private final Pattern pattern;
    private final String replaceWith;
    private int incompleteMatchStartIndex;
    private int charsAfterReplacement;

    BufferContentReplacer(String regex, String replaceWith, int bufferSize) {
        if (regex.isBlank())
            throw new IllegalArgumentException("Invalid regex");
        this.replaceWith = replaceWith;
        this.bufferSize = bufferSize;
        pattern = Pattern.compile(regex);
        incompleteMatchStartIndex = -1;
    }

    char[] replaceMatchesIfAny(final char[] buffer, final int charsInBuffer) {
        resetReplacer(buffer, charsInBuffer);
        if (isMatchingValid()) {
            replaceContent();
            return adjustContentInBuffer();
        }
        return buffer;
    }

    private void resetReplacer(final char[] buffer, final int charsInBuffer) {
        bufferContent = String.valueOf(buffer, 0, charsInBuffer);
        matcher = pattern.matcher(bufferContent);
        charsAfterReplacement = charsInBuffer;
        incompleteMatchStartIndex = -1;
    }

    private boolean isMatchingValid() {
        if (matcher.find()) {
            if (isWholeBufferMatching())
                throw new IllegalStateException("Regex match too broad, increase buffer size");
            return true;
        }
        return false;
    }

    private boolean isWholeBufferMatching() {
        return matcher.end() == bufferSize && matcher.start() == 0;
    }

    private void replaceContent() {
        bufferContent = matcher.replaceAll(matchResult -> {
            if (matchResult.end() == bufferSize) {
                incompleteMatchStartIndex = matcher.start();
                return matchResult.group();
            }
            charsAfterReplacement += replaceWith.length() - matchResult.group().length();
            return replaceWith;
        });
    }

    private char[] adjustContentInBuffer() {
        char[] bufferReplaced;
        if (bufferContent.length() > bufferSize) {
            bufferReplaced = putLongerContentInBuffer();
        } else {
            bufferReplaced = putContentInBuffer();
        }
        return bufferReplaced;
    }

    private char[] putLongerContentInBuffer() {
        if (incompleteMatchStartIndex != -1)
            incompleteMatchStartIndex += bufferContent.length() - bufferSize;
        char[] bufferReplaced = bufferContent.toCharArray();
        bufferSize = bufferReplaced.length;
        return bufferReplaced;
    }

    /*
     * For some reasons matcher.replaceAll() returns a string long 8191 chars
     * instead of 8192 (default buffer size used), these instructions prevent
     * the buffer from shrinking every time.
     * Otherwise replacedContent.toCharArray() could be used in any case and
     * swapLongerContentInBuffer() alone would do the job.
     */
    private char[] putContentInBuffer() {
        char[] bufferReplaced = new char[bufferSize];
        char[] tmpBuffer = bufferContent.toCharArray();
        System.arraycopy(tmpBuffer, 0, bufferReplaced, 0, tmpBuffer.length);
        return bufferReplaced;
    }

    int getCharsAfterReplacement() {
        return charsAfterReplacement;
    }

    boolean isLastMatchIncomplete() {
        return incompleteMatchStartIndex != -1;
    }

    int getIncompleteMatchStartIndex() {
        return incompleteMatchStartIndex;
    }


}
