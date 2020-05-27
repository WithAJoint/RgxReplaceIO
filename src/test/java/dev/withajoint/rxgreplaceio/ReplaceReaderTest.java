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
        initReader("", 0);
    }

    @Test
    public void readSingleChar_noReplacement_worksFine() throws IOException {
        String expected = "test";
        ReplaceReader reader = initReader(expected);
        int readChar;

        readCharByChar(reader);

        assertStringEqualityOutputDifferences(expected);
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

    public void readBuffer_noReplacement_worksFine() throws IOException{
        String expected = "test";
        char[] contentRead = new char[10];
        ReplaceReader reader = initReader(expected);

        reader.read(contentRead, 0, contentRead.length);

        result.append(contentRead);
        assertStringEqualityOutputDifferences(expected);
    }

    @Test
    public void markSupported_returnsTrue() {
        boolean expected = true;
        ReplaceReader replaceReader = initReader("");

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

    private void readBuffer(ReplaceReader reader, char[] buffer, int off, int len) throws IOException {
    }

    private ReplaceReader initReader(String source) {
        return initReader(source, "", "");
    }

    private ReplaceReader initReader(String source, int bufferSize) {
        return initReader(source, "", "", bufferSize);
    }

    private ReplaceReader initReader(String source, String regex, String replaceWith) {
        return new ReplaceReader(new StringReader(source), regex, replaceWith);
    }

    private ReplaceReader initReader(String source, String regex, String replaceWith, int bufferSize) {
        return new ReplaceReader(new StringReader(source), regex, replaceWith, bufferSize);
    }
}
