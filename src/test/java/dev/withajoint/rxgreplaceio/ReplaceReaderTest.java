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
    public void read_singleCharWithoutReplacement_worksFine() throws IOException {
        String expected = "test";
        ReplaceReader reader = initReader(expected);
        int readChar;

        readCharByChar(reader);

        assert expected.contentEquals(result);
    }

    @Test
    public void read_singleCharWordReplacement_worksFine() throws IOException {
        String expected = "test test";
        ReplaceReader reader = initReader("test pippo", "pippo", "test");
        int readChar;

        readCharByChar(reader);

        assert expected.contentEquals(result);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void read_regexMatchWholeBuffer_throwsException() throws IOException {
        ReplaceReader reader = initReader("0123456789", "\\d+", "", 5);

        readCharByChar(reader);
    }

    @Test
    public void read_regexMatchNeedsBufferFill_worksFine() throws IOException {
        String expected = "aaaaa";
        ReplaceReader reader = initReader("aaabb" + "bbaa", "b+", "", 5);

        readCharByChar(reader);

        assert expected.contentEquals(result);
    }

    @Test
    public void markSupported_returnTrue() {
        boolean expected = true;
        ReplaceReader replaceReader = initReader("");

        boolean result = replaceReader.markSupported();

        assert result == expected;
    }

    private void readCharByChar(ReplaceReader reader) throws IOException {
        int readChar;
        while ((readChar = reader.read()) != -1) {
            result.append((char) readChar);
        }
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
