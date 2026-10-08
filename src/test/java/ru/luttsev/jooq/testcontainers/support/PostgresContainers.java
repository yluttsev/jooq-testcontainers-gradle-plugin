package ru.luttsev.jooq.testcontainers.support;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public final class PostgresContainers {

    private static final String IMAGE = "postgres:18";

    private PostgresContainers() {
    }

    public static Set<String> running() throws IOException, InterruptedException {
        Process process = new ProcessBuilder("docker", "ps", "--filter", "ancestor=" + IMAGE,
                "--format", "{{.ID}}").redirectErrorStream(true).start();
        Set<String> ids;
        try (var output = process.inputReader()) {
            ids = output.lines().filter(line -> !line.isBlank()).collect(Collectors.toSet());
        }
        if (process.waitFor() != 0) {
            throw new IOException("Could not inspect PostgreSQL containers: " + ids);
        }
        return ids;
    }
}
