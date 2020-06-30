package dev.withajoint.rgxreplaceio;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class ReplaceWriterTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void instantiation_bufferSizeLessThanOrEqualTo0_throwException() {
        initWriter(new StringWriter(), 0);
    }

    @Test
    public void write_oneCharMatchingRegex_writeReplacement() throws IOException {
        StringWriter out = new StringWriter();
        String expected = "b";
        ReplaceWriter writer = initWriter(out, ".+", expected);

        writeCharsAndFlush(writer, "a".toCharArray());

        String result = out.toString();
        assertStringEqualityOutputDifferences(expected, result);
    }

    @Test
    public void write_charsExceedBufferLength_flushAutomaticallyWhenBufferIsFull() throws IOException {
        StringWriter out = new StringWriter();
        String expected = "abcdefgh";
        ReplaceWriter writer = initWriter(out, 5);

        writeCharsAndFlush(writer, expected.toCharArray());

        assertStringEqualityOutputDifferences(expected, out.toString());
    }

    @Test
    public void write_longerReplacement_writeFollowingCharsAtRightIndex() throws IOException {
        StringWriter out = new StringWriter();
        String input = "abc12";
        String followingInput = "de";
        String expected = "abc####de";
        ReplaceWriter writer = initWriter(out, "\\d", "##");

        writeCharsAndFlush(writer, input.toCharArray());
        writeCharsAndFlush(writer, followingInput.toCharArray());

        assertStringEqualityOutputDifferences(expected, out.toString());
    }

    private ReplaceWriter initWriter(Writer out) {
        return initWriter(out, "uselessForThisTest", "");
    }

    private ReplaceWriter initWriter(Writer out, int bufferSize) {
        return initWriter(out, "uselessForThisTest", "", bufferSize);
    }

    private ReplaceWriter initWriter(Writer out, String regex, String replaceWith) {
        return new ReplaceWriter(out, regex, replaceWith);
    }

    private ReplaceWriter initWriter(Writer out, String regex, String replaceWith, int bufferSize) {
        return new ReplaceWriter(out, regex, replaceWith, bufferSize);
    }

    private void writeCharsAndFlush(Writer writer, char[] charsToWrite) throws IOException {
        for (char c : charsToWrite)
            writer.write(c);
        writer.flush();
    }

    //duplicate declaration in ReplaceReaderTest, TODO: move it to its own util class
    private void assertStringEqualityOutputDifferences(String expected, String result) {
        assert expected.contentEquals(result) : "expected: " + expected + " result: " + result;
    }

}
