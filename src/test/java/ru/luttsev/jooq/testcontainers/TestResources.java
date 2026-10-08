package ru.luttsev.jooq.testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class TestResources {

    private TestResources() {
    }

    static String readResource(String path) throws IOException {
        try (InputStream input = TestResources.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Missing test resource: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
