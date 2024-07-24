package com.nextbreakpoint.nextfractal.core.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtils {
    public static void copyBytes(InputStream is, OutputStream os) throws IOException {
        final byte[] data = new byte[8192];
        int length;
        while ((length = is.read(data)) > 0) {
            os.write(data, 0, length);
        }
    }

    public static String readString(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            copyBytes(is, os);
            return os.toString();
        }
    }
}
