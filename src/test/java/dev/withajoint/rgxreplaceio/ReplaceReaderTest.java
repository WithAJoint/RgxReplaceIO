package dev.withajoint.rgxreplaceio;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ReplaceReaderTest {


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void instantiation_bufferSizeLessThanOrEqualTo0_throwException() {
        initReader("source", "regex", "", 0);
    }

    @Test
    public void replacement_matchFound_replaceMatch() throws IOException {
        String expected = "test test";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("test pippo", "pippo", "test");

        readCharByChar(reader, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void replacement_replacingExceedBufferLength_replaceAnyways() throws IOException {
        String expected = "abcdefg";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("adefg", "a", "abc", 5);

        readCharByChar(reader, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void replacement_regexMatchWholeBuffer_throwException() throws IOException {
        ReplaceReader reader = initReader("0123456789", "\\d+", "", 5);

        readCharByChar(reader, new StringBuilder());
    }

    @Test
    public void replacement_regexMatchUntilEndOfBuffer_replaceMatch() throws IOException {
        String expected = "aaa" + "ccccc";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("aaabb", "b+", "ccccc", 5);

        readCharByChar(reader, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void replacement_bufferHoldsPartialMatch_replaceMatch() throws IOException {
        String expected = "aaaaa";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("aaabb" + "bbaa", "b+", "", 5);

        readCharByChar(reader, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readIntoBuffer_bufferHoldsPartialMatch_replaceMatch() throws IOException {
        String expected = "12345";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("123abc45", "[a-z]+", "", 5);

        readIntoBuffer(reader, 5, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readIntoBuffer_longerReplacement_replaceMatches() throws IOException {
        String expected = "123666674666675";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("123abc4abcd5", "[a-z]+", "66667", 5);

        readIntoBuffer(reader, 50, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readIntoBuffer_charactersToReadOvercomeInputLength_readUntilContentLength() throws IOException {
        String expected = "test";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader(expected);

        readIntoBuffer(reader, 20, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readIntoBuffer_charactersToReadOvercomeBufferSize_readAnyways() throws IOException {
        String expected = "123456789";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader(expected, 3);

        readIntoBuffer(reader, expected.length(), result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_noCharsToRead_returnNull() throws IOException {
        String expected = null;
        ReplaceReader reader = initReader("");

        String result = reader.readLine();

        assert expected == result;
    }

    @Test
    public void readLine_endOfFileAsDelimiter_readWholeContent() throws IOException {
        String expected = "12345";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("12345");

        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_lineFeedAsDelimiter_readFirstLine() throws IOException {
        String expected = "12345";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("12345\n6789");

        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_lineFeedAsDelimiter_readSecondLine() throws IOException {
        String expected = "6789";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("12345\n6789");

        reader.readLine();
        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_carriageReturnAsDelimiter_readFirstLine() throws IOException {
        String expected = "12345";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("12345\r6789");

        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_carriageReturnAsDelimiter_readSecondLine() throws IOException {
        String expected = "6789";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("12345\r6789");

        reader.readLine();
        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_crlfAsDelimiter_readSecondLine() throws IOException {
        String expected = "6789";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("12345\r\n6789");

        reader.readLine();
        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_lineWithoutChars_returnEmptyString() throws IOException {
        String expected = "";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("12\n\n345");

        reader.readLine();
        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_lineBiggerThanBufferSize_readAnyways() throws IOException {
        String expected = "abcdefgh";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("abcdefgh", 3);

        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void readLine_lineContainsIncompleteMatch_readReplacedContent() throws IOException {
        String expected = "abcde";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("abc0001de\nfgh", "\\d+", "", 5);

        result.append(reader.readLine());

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void read_readIntoBufferAfterReadingSomeChars_readContent() throws IOException {
        String expected = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader(expected);

        readCharByChar(reader, 4, result);
        readIntoBuffer(reader, 22, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void read_readSomeCharsAfterReadingBuffer_readContent() throws IOException {
        String expected = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader(expected);

        readIntoBuffer(reader, 20, result);
        readCharByChar(reader, 7, result);

        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void skip_skipSomeChars_contentReadMissesThoseChars() throws IOException {
        String expected = "abcghi";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("abcdefghi");

        readCharByChar(reader, 3, result);
        long skippedCharsCount = reader.skip(3);
        readCharByChar(reader, result);

        assert skippedCharsCount == 3;
        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void skip_skipMoreCharsThanBufferContains_contentReadMissesThoseChars() throws IOException {
        String expected = "abcjkl";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("abcdefghijkl", 3);

        readCharByChar(reader, 3, result);
        long skippedCharsCont = reader.skip(6);
        readCharByChar(reader, result);

        assert skippedCharsCont == 6;
        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void skip_charsSkippedNumberNotMultipleOfBufferSizeButBigger_contentReadMissesThoseChars() throws IOException {
        String expected = "abckl";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("abcdefghijkl", 3);

        readCharByChar(reader, 3, result);
        long skippedCharsCount = reader.skip(7);
        readCharByChar(reader, result);

        assert skippedCharsCount == 7;
        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void skip_skipMoreCharsThanInputLength_skippedCharsCountEqualsInputLength() throws IOException {
        String expected = "abc";
        StringBuilder result = new StringBuilder();
        ReplaceReader reader = initReader("abcdefghijkl");

        readCharByChar(reader, 3, result);
        long skippedCharsCount = reader.skip(20);
        readCharByChar(reader, result);

        assert skippedCharsCount == 9;
        assertStringEqualityOutputDifferences(expected, result.toString());
    }

    @Test
    public void ready_inputEmpty_returnFalse() throws IOException {
        boolean expected = false;
        ReplaceReader reader = initReader("");

        boolean result = reader.ready();

        assert result == expected;
    }

    @Test
    public void ready_inputNotEmpty_returnTrue() throws IOException {
        boolean expected = true;
        ReplaceReader reader = initReader("input");

        boolean result = reader.ready();

        assert result == expected;
    }

    @Test
    public void markSupport_returnFalse() {
        boolean expected = false;
        ReplaceReader replaceReader = initReader("source");

        boolean result = replaceReader.markSupported();

        assert result == expected;
    }

    private void assertStringEqualityOutputDifferences(String expected, String result) {
        assert expected.contentEquals(result) : "expected: " + expected + " result: " + result;
    }

    private void readCharByChar(Reader reader, StringBuilder result) throws IOException {
        //-1 read wihout limit
        readCharByChar(reader, -1, result);
    }

    private void readCharByChar(Reader reader, int charactersToRead, StringBuilder result) throws IOException {
        int readChar, charsRead = 0;
        while ((readChar = reader.read()) != -1) {
            charsRead++;
            result.append((char) readChar);
            if (charsRead == charactersToRead)
                break;
        }
    }

    private void readIntoBuffer(Reader reader, int charactersToRead, StringBuilder result) throws IOException {
        char[] contentRead = new char[charactersToRead];
        int charsRead = reader.read(contentRead, 0, charactersToRead);
        result.append(contentRead, 0, charsRead);
    }

    private ReplaceReader initReader(String source) {
        return initReader(source, "uselessForThisTest", "");
    }

    private ReplaceReader initReader(String source, int bufferSize) {
        return initReader(source, "uselessForThisTest", "", bufferSize);
    }

    private ReplaceReader initReader(String source, String regex, String replaceWith) {
        return new ReplaceReader(new StringReader(source), regex, replaceWith);
    }

    private ReplaceReader initReader(String source, String regex, String replaceWith, int bufferSize) {
        return new ReplaceReader(new StringReader(source), regex, replaceWith, bufferSize);
    }
}
