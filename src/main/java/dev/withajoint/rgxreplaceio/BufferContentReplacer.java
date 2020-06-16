package dev.withajoint.rgxreplaceio;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BufferContentReplacer {

    private String bufferContent;
    private Matcher matcher;
    private final Pattern pattern;
    private final String replaceWith;
    private int incompleteMatchStartIndex;
    private int charsAfterReplacement;

    BufferContentReplacer(String regex, String replaceWith) {
        if (regex.isBlank())
            throw new IllegalArgumentException("Invalid regex");
        this.replaceWith = replaceWith;
        pattern = Pattern.compile(regex);
        incompleteMatchStartIndex = -1;
    }

    char[] replaceMatchesIfAny(final char[] buffer, final int charsInBuffer) {
        resetReplacer(buffer, charsInBuffer);
        if (matcher.find()) {
            if (isWholeBufferMatching(buffer))
                throw new IllegalStateException("Regex match too broad, increase buffer size");
            String replacedContent = replaceContent(buffer);
            if (isReplacementValid(replacedContent)) {
                return replaceBuffer(buffer, replacedContent);
            }
        }
        return buffer;
    }

    private void resetReplacer(char[] buffer, int charsInBuffer) {
        bufferContent = String.valueOf(buffer, 0, charsInBuffer);
        matcher = pattern.matcher(bufferContent);
        charsAfterReplacement = charsInBuffer;
        incompleteMatchStartIndex = -1;
    }

    private boolean isWholeBufferMatching(char[] buffer) {
        return matcher.end() == buffer.length && matcher.start() == 0;
    }

    private String replaceContent(char[] buffer) {
        return matcher.replaceAll(matchResult -> {
            if (matchResult.end() == buffer.length) {
                incompleteMatchStartIndex = matcher.start();
                return matchResult.group();
            }
            charsAfterReplacement += replaceWith.length() - matchResult.group().length();
            return replaceWith;
        });
    }

    private boolean isReplacementValid(String replacedContent) {
        return !replacedContent.contentEquals(bufferContent) && charsAfterReplacement > 0;
    }

    private char[] replaceBuffer(char[] buffer, String replacedContent) {
        char[] bufferAfterReplacement;
        if (replacedContent.length() > buffer.length) {
            bufferAfterReplacement = swapLongerContent(buffer, replacedContent);
        } else {
            bufferAfterReplacement = swapContent(buffer, replacedContent);
        }
        return bufferAfterReplacement;
    }

    private char[] swapLongerContent(char[] buffer, String replacedContent) {
        if (incompleteMatchStartIndex != -1)
            incompleteMatchStartIndex += replacedContent.length() - buffer.length;
        return replacedContent.toCharArray();
    }

    /*
     * For some reasons matcher.replaceAll() returns a string long 8191 chars
     * instead of 8192 (default buffer size used), these instructions prevent
     * the buffer from shrinking every time.
     * Otherwise replacedContent.toCharArray() could be used in any case and
     * swapLongerContent() alone would do the job.
     */
    private char[] swapContent(char[] buffer, String replacedContent) {
        char[] tmpBuffer = new char[buffer.length];
        char[] newContent = replacedContent.toCharArray();
        System.arraycopy(newContent, 0, tmpBuffer, 0, newContent.length);
        return tmpBuffer;
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
