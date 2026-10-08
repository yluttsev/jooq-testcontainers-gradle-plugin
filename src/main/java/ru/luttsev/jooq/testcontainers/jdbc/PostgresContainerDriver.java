package ru.luttsev.jooq.testcontainers.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class PostgresContainerDriver implements Driver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresContainerDriver.class);

    @Override
    public boolean acceptsURL(String url) {
        return url != null && url.startsWith(ConnectionOptions.URL_PREFIX);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        ConnectionOptions options = ConnectionOptions.fromUrl(url);
        PostgreSQLContainer<?> container = createContainer(options);

        long startedAt = System.nanoTime();
        if (options.logs(Level.INFO)) {
            LOGGER.info("Starting PostgreSQL container: {}", options.image());
        }
        try {
            container.start();
            Connection connection = container.createConnection("");
            if (options.logs(Level.INFO)) {
                LOGGER.info("PostgreSQL ready in {} ms", elapsedMillis(startedAt));
            }
            return ManagedConnection.wrap(connection, container, options);
        } catch (RuntimeException | Error | SQLException failure) {
            stopAfterFailure(container, options, failure);
            throw failure;
        }
    }

    private static PostgreSQLContainer<?> createContainer(ConnectionOptions options) {
        return new PostgreSQLContainer<>(DockerImageName.parse(options.image())
                .asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("codegen")
                .withUsername("codegen")
                .withPassword("codegen")
                .withStartupTimeoutSeconds(options.startupTimeoutSeconds());
    }

    private static void stopAfterFailure(PostgreSQLContainer<?> container,
                                         ConnectionOptions options,
                                         Throwable failure) {
        if (options.logs(Level.ERROR)) {
            LOGGER.error("Failed to prepare PostgreSQL container", failure);
        }
        try {
            container.stop();
        } catch (RuntimeException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
