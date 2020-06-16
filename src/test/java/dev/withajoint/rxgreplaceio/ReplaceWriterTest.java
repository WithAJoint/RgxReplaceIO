package dev.withajoint.rxgreplaceio;

import dev.withajoint.rgxreplaceio.ReplaceWriter;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;

public class ReplaceWriterTest {

    public void write_oneChar_write() throws IOException {
        StringWriter out = new StringWriter();
        ReplaceWriter writer = new ReplaceWriter(out, "a", "b");

        writer.write("a");

        assert out.toString().contentEquals("b") : "expected : 'b' result: 'a'";
    }
}
