package ru.luttsev.jooq.testcontainers.migration;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record LiquibaseOptions(
        String changeLog,
        List<Path> searchPaths,
        String contexts,
        String labels,
        Map<String, String> parameters
) {

    public LiquibaseOptions {
        searchPaths = List.copyOf(searchPaths);
        parameters = Map.copyOf(parameters);
    }
}
