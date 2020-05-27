package dev.withajoint.rxgreplaceio;

import dev.withajoint.rgxreplaceio.ReplaceReader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;

public class ReplaceReaderTest {

    private StringBuilder result;

    @BeforeMethod
    private void setup() {
        result = new StringBuilder();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_bufferSizeLessThanOrEqualTo0_throwsException() {
        initReader("source", "regex", "",  0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void readSingleChar_noReplacement_throwsException() throws IOException {
        ReplaceReader reader = initReader("source", "", "");
    }

    @Test
    public void readSingleChar_wordReplacement_worksFine() throws IOException {
        String expected = "test test";
        ReplaceReader reader = initReader("test pippo", "pippo", "test");
        int readChar;

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readSingleChar_wordReplacementResultBiggerThanBuffer_worksFine() throws IOException {
        String expected = "abcdefg";
        ReplaceReader reader = initReader("adefg", "a", "abc", 5);

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void readSingleChar_regexMatchWholeBuffer_throwsException() throws IOException {
        ReplaceReader reader = initReader("0123456789", "\\d+", "", 5);

        readCharByChar(reader);
    }

    @Test
    public void readSingleChar_regexMatchUntilEndOfBuffer_worksFine() throws IOException {
        String expected = "aaaaa";
        ReplaceReader reader = initReader("aaabb" + "bbaa", "b+", "", 5);

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readSingleChar_regexMatchUntilEndOfInput_worksFine() throws IOException {
        String expected = "aaa";
        ReplaceReader reader = initReader("aaabb", "b+", "");

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readSingleChar_regexMatchUntilEndOfBufferEndOfInput_worksFine() throws IOException {
        String expected = "aaa";
        ReplaceReader reader = initReader("aaabb", "b+", "", 5);

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readBuffer_inputLengthEqualToCharactersToRead_worksFine() throws IOException {
        String expected = "test";
        ReplaceReader reader = initReader(expected, "regex", "");

        readBuffer(reader, 4);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readBuffer_inputLengthSmallerThanCharactersToRead_worksFine() throws IOException {
        String expected = "test";
        ReplaceReader reader = initReader(expected, "regex", "");

        readBuffer(reader, 20);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void readBuffer_charactersToReadBiggerThanReaderBuffer_worksFine() throws IOException {
        String expected = "0123456789";
        ReplaceReader reader = initReader(expected, "regex", "", 3);

        readBuffer(reader, 10);

        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void markSupported_returnsTrue() {
        boolean expected = true;
        ReplaceReader replaceReader = initReader("source", "regex", "");

        boolean result = replaceReader.markSupported();

        assert result == expected;
    }

    private void assertStringEqualityOutputDifferences(String expected) {
        assert expected.contentEquals(result) : "expected: " + expected + " result: " + result;
    }

    private void readCharByChar(ReplaceReader reader) throws IOException {
        int readChar;
        while ((readChar = reader.read()) != -1) {
            result.append((char) readChar);
        }
    }

    private void readBuffer(ReplaceReader reader, int charactersToRead) throws IOException {
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
