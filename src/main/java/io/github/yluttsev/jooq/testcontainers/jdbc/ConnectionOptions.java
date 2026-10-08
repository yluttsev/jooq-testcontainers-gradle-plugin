package io.github.yluttsev.jooq.testcontainers.jdbc;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.event.Level;
import io.github.yluttsev.jooq.testcontainers.migration.LiquibaseOptions;

record ConnectionOptions(
        String image,
        int startupTimeoutSeconds,
        Level logLevel,
        boolean loggingEnabled,
        LiquibaseOptions liquibase
) {

    static final String URL_PREFIX = "jdbc:jooq-testcontainers:postgresql:///codegen?";

    static ConnectionOptions fromUrl(String url) throws SQLException {
        Map<String, String> options = parseQuery(url.substring(URL_PREFIX.length()));
        return new ConnectionOptions(
                required(options, "image"),
                positiveInt(required(options, "startupTimeoutSeconds")),
                Level.valueOf(required(options, "logLevel")),
                Boolean.parseBoolean(required(options, "loggingEnabled")),
                liquibaseOptions(options));
    }

    boolean logs(Level level) {
        return loggingEnabled && logLevel.toInt() <= level.toInt();
    }

    private static Map<String, String> parseQuery(String query) throws SQLException {
        Map<String, String> options = new HashMap<>();
        for (String item : query.split("&")) {
            String[] pair = item.split("=", 2);
            if (pair.length != 2) {
                throw new SQLException("Invalid PostgreSQL JDBC option: " + item);
            }
            options.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
        }
        return options;
    }

    private static LiquibaseOptions liquibaseOptions(Map<String, String> options) throws SQLException {
        List<Path> searchPaths = new ArrayList<>();
        for (int index = 0; options.containsKey("searchPath." + index); index++) {
            searchPaths.add(Path.of(required(options, "searchPath." + index)));
        }
        if (searchPaths.isEmpty()) {
            throw new SQLException("Liquibase search path must not be empty");
        }

        Map<String, String> parameters = new HashMap<>();
        options.forEach((key, value) -> {
            if (key.startsWith("parameter.")) {
                parameters.put(key.substring("parameter.".length()), value);
            }
        });
        return new LiquibaseOptions(
                required(options, "changeLog"),
                searchPaths,
                options.getOrDefault("contexts", ""),
                options.getOrDefault("labels", ""),
                parameters
        );
    }

    private static String required(Map<String, String> options, String key) throws SQLException {
        String value = options.get(key);
        if (value == null || value.isBlank()) {
            throw new SQLException("Missing PostgreSQL JDBC option: " + key);
        }
        return value;
    }

    private static int positiveInt(String value) throws SQLException {
        try {
            int result = Integer.parseInt(value);
            if (result > 0) {
                return result;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new SQLException("Invalid PostgreSQL startup timeout: " + value);
    }
}
