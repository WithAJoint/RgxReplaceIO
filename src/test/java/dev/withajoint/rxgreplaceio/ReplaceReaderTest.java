package dev.withajoint.rxgreplaceio;

import dev.withajoint.rgxreplaceio.ReplaceReader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ReplaceReaderTest {

    private StringBuilder result;

    @BeforeMethod
    private void setup() {
        result = new StringBuilder();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void instantiation_bufferSizeLessThanOrEqualTo0_throwException() {
        initReader("source", "regex", "", 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void instantiation_emptyRegex_throwException() throws IOException {
        ReplaceReader reader = initReader("source", "", "");
    }

    @Test
    public void replacement_matchFound_replaceMatch() throws IOException {
        String expected = "test test";
        ReplaceReader reader = initReader("test pippo", "pippo", "test");

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void replacement_replacingExceedBufferLength_replaceAnyways() throws IOException {
        String expected = "abcdefg";
        ReplaceReader reader = initReader("adefg", "a", "abc", 5);

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void replacement_regexMatchWholeBuffer_throwException() throws IOException {
        ReplaceReader reader = initReader("0123456789", "\\d+", "", 5);

        readCharByChar(reader);
    }

    @Test
    public void replacement_regexMatchUntilEndOfBuffer_replaceMatch() throws IOException {
        String expected = "aaa" + "ccccc";
        ReplaceReader reader = initReader("aaabb", "b+", "ccccc", 5);

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void replacement_bufferHoldsPartialMatch_replaceMatch() throws IOException {
        String expected = "aaaaa";
        ReplaceReader reader = initReader("aaabb" + "bbaa", "b+", "", 5);

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readBuffer_bufferHoldsPartialMatch_replaceMatch() throws IOException {
        String expected = "12345";
        ReplaceReader reader = initReader("123abc45", "[a-z]+", "", 5);

        readBuffer(reader, 5);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readBuffer_longerReplacement_replaceMatches() throws IOException {
        String expected = "123666674666675";
        ReplaceReader reader = initReader("123abc4abcd5", "[a-z]+", "66667", 5);

        readBuffer(reader, 50);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readBuffer_charactersToReadOvercomeInputLength_readUntilContentLength() throws IOException {
        String expected = "test";
        ReplaceReader reader = initReader(expected, "uselessForThisTest", "");

        readBuffer(reader, 20);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readBuffer_charactersToReadOvercomeBufferSize_readAnyways() throws IOException {
        String expected = "123456789";
        ReplaceReader reader = initReader(expected, "uselessForThisTest", "", 3);

        readBuffer(reader, expected.length());

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void read_readBufferAfterReadingSomeChars_readContent() throws IOException {
        String expected = "abcdefghijklmnopqrstuvwxyz";
        ReplaceReader reader = initReader(expected, "uselessForThisTest", "");

        readCharByChar(reader, 4);
        readBuffer(reader, 22);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void read_readSomeCharsAfterReadingBuffer_readContent() throws IOException {
        String expected = "abcdefghijklmnopqrstuvwxyz";
        ReplaceReader reader = initReader(expected, "uselessForThisTest", "");

        readBuffer(reader, 20);
        readCharByChar(reader, 7);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void skip_skipSomeChars_contentReadMissesThoseChars() throws IOException {
        String expected = "abcghi";
        ReplaceReader reader = initReader("abcdefghi", "uselessForThisTest", "");

        readCharByChar(reader, 3);
        long skippedCharsCount = reader.skip(3);
        readCharByChar(reader);

        assert skippedCharsCount == 3;
        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void skip_skipMoreCharsThanBufferContains_contentReadMissesThoseChars() throws IOException {
        String expected = "abcjkl";
        ReplaceReader reader = initReader("abcdefghijkl", "uselessForThisTest", "", 3);

        readCharByChar(reader, 3);
        long skippedCharsCont = reader.skip(6);
        readCharByChar(reader);

        assert skippedCharsCont == 6;
        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void skip_charsSkippedNumberNotMultipleOfBufferSizeButBigger_contentReadMissesThoseChars() throws IOException {
        String expected = "abckl";
        ReplaceReader reader = initReader("abcdefghijkl", "uselessForThisTest", "", 3);

        readCharByChar(reader, 3);
        long skippedCharsCount = reader.skip(7);
        readCharByChar(reader);

        assert skippedCharsCount == 7;
        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void skip_skipMoreCharsThanInputLength_skippedCharsCountEqualsInputLength() throws IOException {
        String expected = "abc";
        ReplaceReader reader = initReader("abcdefghijkl", "uselessForThisTest", "");

        readCharByChar(reader, 3);
        long skippedCharsCount = reader.skip(20);
        readCharByChar(reader);

        assert skippedCharsCount == 9;
        assertStringEqualityOutputDifferences(expected);
    }

   @Test
    public void markSupport_returnFalse() {
        boolean expected = false;
        ReplaceReader replaceReader = initReader("source", "regex", "");

        boolean result = replaceReader.markSupported();

        assert result == expected;
    }

    private void assertStringEqualityOutputDifferences(String expected) {
        assert expected.contentEquals(result) : "expected: " + expected + " result: " + result;
    }

    private void readCharByChar(Reader reader) throws IOException {
        //-1 read wihout limit
        readCharByChar(reader, -1);
    }

    private void readCharByChar(Reader reader, int charactersToRead) throws IOException {
        int readChar, charsRead = 0;

        while ((readChar = reader.read()) != -1) {
            charsRead++;
            result.append((char) readChar);
            if (charsRead == charactersToRead)
                break;
        }

    }

    private void readBuffer(Reader reader, int charactersToRead) throws IOException {
        char[] contentRead = new char[charactersToRead];
        int charsRead = reader.read(contentRead, 0, charactersToRead);
        result.append(contentRead, 0, charsRead);
    }

    private ReplaceReader initReader(String source, String regex, String replaceWith) {
        return new ReplaceReader(new StringReader(source), regex, replaceWith);
    }

    private ReplaceReader initReader(String source, String regex, String replaceWith, int bufferSize) {
        return new ReplaceReader(new StringReader(source), regex, replaceWith, bufferSize);
    }
}
