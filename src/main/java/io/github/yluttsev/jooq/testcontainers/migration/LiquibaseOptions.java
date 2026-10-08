package io.github.yluttsev.jooq.testcontainers.migration;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Values passed from the Gradle extension to Liquibase at code generation time.
 *
 * @param changeLog path of the root changelog
 * @param searchPaths roots used to find changelogs and included resources
 * @param contexts context filter, or an empty string
 * @param labels label filter, or an empty string
 * @param parameters changelog parameters
 */
public record LiquibaseOptions(
        String changeLog,
        List<Path> searchPaths,
        String contexts,
        String labels,
        Map<String, String> parameters
) {

    /** Copies the search paths and parameters into immutable collections. */
    public LiquibaseOptions {
        searchPaths = List.copyOf(searchPaths);
        parameters = Map.copyOf(parameters);
    }
}
