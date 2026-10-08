package ru.luttsev.jooq.testcontainers.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestResources {

    private TestResources() {
    }

    public static String readResource(String path) throws IOException {
        try (InputStream input = TestResources.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Missing test resource: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void copyResource(String resource, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        Files.writeString(destination, readResource(resource));
    }
}
