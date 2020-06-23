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

    BufferContentReplacer(String regex, String replaceWith) {
        if (regex.isBlank())
            throw new IllegalArgumentException("Invalid regex");
        this.replaceWith = replaceWith;
        pattern = Pattern.compile(regex);
        incompleteMatchStartIndex = -1;
    }

    char[] replaceMatchesIfAny(char[] buffer, final int charsInBuffer) {
        resetReplacer(buffer, charsInBuffer);
        if (isMatchingValid()) {
            replaceContent();
            buffer = adjustContentInBuffer();
        }
        return buffer;
    }

    private void resetReplacer(final char[] buffer, final int charsInBuffer) {
        bufferContent = String.valueOf(buffer, 0, charsInBuffer);
        bufferSize = buffer.length;
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
        return bufferContent.length() > bufferSize ? putLongerContentInBuffer() : putContentInBuffer();
    }

    private char[] putLongerContentInBuffer() {
        if (incompleteMatchStartIndex != -1)
            incompleteMatchStartIndex += bufferContent.length() - bufferSize;
        return bufferContent.toCharArray();
    }

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
