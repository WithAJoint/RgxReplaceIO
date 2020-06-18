package dev.withajoint.rgxreplaceio;

import org.testng.annotations.Test;

import java.io.IOException;

public class BufferContentReplacerTest {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void instantiation_emptyRegex_throwException() throws IOException {
        BufferContentReplacer contentReplacer = new BufferContentReplacer("", "", DEFAULT_BUFFER_SIZE);
    }


}
