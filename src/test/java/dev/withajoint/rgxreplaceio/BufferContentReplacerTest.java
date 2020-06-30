package dev.withajoint.rgxreplaceio;

import org.testng.annotations.Test;

public class BufferContentReplacerTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void instantiation_emptyRegex_throwException() {
        new BufferContentReplacer("", "");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void replacement_regexMatchWholeBuffer_throwException() {
        char[] buffer = {'1', '2', '3'};
        BufferContentReplacer contentReplacer = new BufferContentReplacer("\\d+", "");

        contentReplacer.replaceMatchesIfAny(buffer, buffer.length);
    }

    @Test
    public void replacement_noMatchesFound_returnBufferWithoutChanges() {
        char[] buffer = {'1', '2', '3'};
        BufferContentReplacer contentReplacer = new BufferContentReplacer("0+", "");

        char[] bufferReplaced = contentReplacer.replaceMatchesIfAny(buffer, buffer.length);
        int charsInBuffer = contentReplacer.getCharsAfterReplacement();

        assertReplacement(buffer, buffer.length, bufferReplaced, charsInBuffer);
    }

    @Test
    public void replacement_matchFound_replaceContentInBuffer() {
        char[] buffer = {'a', '0', '0', 'd'};
        char[] expected = {'a', 'b', 'c', 'd'};
        BufferContentReplacer contentReplacer = new BufferContentReplacer("00", "bc");

        char[] bufferReplaced = contentReplacer.replaceMatchesIfAny(buffer, buffer.length);
        int charsInBuffer = contentReplacer.getCharsAfterReplacement();

        assertReplacement(expected, expected.length, bufferReplaced, charsInBuffer);
    }

    @Test
    public void replacement_replaceWithMoreCharsThanMatched_returnLongerBuffer() {
        char[] buffer = {'a', '0', 'e'};
        char[] expected = {'a', 'b', 'c', 'd', 'e'};
        BufferContentReplacer contentReplacer = new BufferContentReplacer("0", "bcd");

        char[] bufferReplaced = contentReplacer.replaceMatchesIfAny(buffer, buffer.length);
        int charsInBuffer = contentReplacer.getCharsAfterReplacement();

        assertReplacement(expected, expected.length, bufferReplaced, charsInBuffer);
    }

    @Test
    public void replacement_replaceWithLessCharsThanMatched_returnSameLengthBuffer() {
        char[] buffer = {'a', 'b', 'c', '0', '0', 'd', 'e'};
        char[] expected = {'a', 'b', 'c', 'd', 'e', 0, 0};
        int expectedCharsInBuffer = 5;
        BufferContentReplacer contentReplacer = new BufferContentReplacer("0+", "");

        char[] bufferReplaced = contentReplacer.replaceMatchesIfAny(buffer, buffer.length);
        int charsInBuffer = contentReplacer.getCharsAfterReplacement();

        assertReplacement(expected, expectedCharsInBuffer, bufferReplaced, charsInBuffer);
    }

    @Test
    public void replacement_incompleteMatchFound_returnBufferWithoutChanges() {
        char[] buffer = {'a', 'b', '0'};
        BufferContentReplacer contentReplacer = new BufferContentReplacer("0", "c");

        char[] bufferReplaced = contentReplacer.replaceMatchesIfAny(buffer, buffer.length);
        int charsInBuffer = contentReplacer.getCharsAfterReplacement();

        assertReplacement(buffer, buffer.length, bufferReplaced, charsInBuffer);
    }

    @Test
    public void incompleteMatchState_noIncompleteMatch_returnDefault() {
        char[] buffer = {'a', '0', 'e'};
        int expectedStartIndex = -1;
        BufferContentReplacer contentReplacer = new BufferContentReplacer("0", "");

        contentReplacer.replaceMatchesIfAny(buffer, buffer.length);

        assert !contentReplacer.isLastMatchIncomplete();
        assert contentReplacer.getIncompleteMatchStartIndex() == expectedStartIndex;
    }

    @Test
    public void incompleteMatchState_incompleteMatchFound_returnStartingIndex() {
        char[] buffer = {'a', 'b', '0'};
        int expectedStartIndex = 2;
        BufferContentReplacer contentReplacer = new BufferContentReplacer("0", "c");

        contentReplacer.replaceMatchesIfAny(buffer, buffer.length);

        assert contentReplacer.isLastMatchIncomplete();
        assert contentReplacer.getIncompleteMatchStartIndex() == expectedStartIndex;
    }

    @Test
    public void incompleteMatchState_longerReplacementAndIncompleteMatch_returnAdjustedStartIndex() {
        char[] buffer = {'0', 'a', '0', '0', 'b'};
        int expectedStartIndex = 6;
        BufferContentReplacer contentReplacer = new BufferContentReplacer("[a-z]", "000");

        contentReplacer.replaceMatchesIfAny(buffer, buffer.length);

        assert contentReplacer.isLastMatchIncomplete();
        assert contentReplacer.getIncompleteMatchStartIndex() == expectedStartIndex;
    }

    @Test
    public void incompleteMatchState_partialMatchAtEndOfBufferNotMatchingYet_returnStartingIndex() {
        char[] buffer = {'n', 'o', ' ', '3', '3', '3', '-', '4'};
        int expectedStartIndex = 3;
        BufferContentReplacer contentReplacer = new BufferContentReplacer("\\d{3}-\\d{7}\\b", "###-#######");

        contentReplacer.replaceMatchesIfAny(buffer, buffer.length);

        assert contentReplacer.isLastMatchIncomplete();
        assert contentReplacer.getIncompleteMatchStartIndex() == expectedStartIndex;
    }


    private void assertReplacement(char[] expectedBuffer, int expectedCharsInBuffer, char[] bufferReplaced, int charsInBuffer) {
        assert expectedBuffer.length == bufferReplaced.length;
        assert expectedCharsInBuffer == charsInBuffer;
        for (int i = 0; i < expectedBuffer.length; i++)
            assert expectedBuffer[i] == bufferReplaced[i];
    }

}
