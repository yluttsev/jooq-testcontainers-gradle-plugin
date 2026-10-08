package ru.luttsev.jooq.testcontainers.jdbc;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.event.Level;

record ConnectionOptions(String image, int startupTimeoutSeconds, Level logLevel, boolean loggingEnabled) {

    static final String URL_PREFIX = "jdbc:jooq-testcontainers:postgresql:///codegen?";

    static ConnectionOptions fromUrl(String url) throws SQLException {
        Map<String, String> options = parseQuery(url.substring(URL_PREFIX.length()));
        return new ConnectionOptions(
                required(options, "image"),
                positiveInt(required(options, "startupTimeoutSeconds")),
                Level.valueOf(required(options, "logLevel")),
                Boolean.parseBoolean(required(options, "loggingEnabled")));
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
            options.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
        }
        return options;
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
