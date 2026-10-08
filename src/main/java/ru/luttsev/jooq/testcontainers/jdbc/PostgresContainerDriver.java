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
import ru.luttsev.jooq.testcontainers.migration.LiquibaseMigrationRunner;

/**
 * JDBC driver used by jOOQ code generation to start and manage a temporary
 * PostgreSQL container. Closing the returned connection stops the container.
 */
public final class PostgresContainerDriver implements Driver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresContainerDriver.class);

    /** Creates the driver loaded by the jOOQ code generator. */
    public PostgresContainerDriver() {
    }

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
            if (options.logs(Level.INFO)) {
                LOGGER.info("PostgreSQL ready in {} ms", elapsedMillis(startedAt));
            }
            applyMigrations(container, options);
            Connection connection = container.createConnection("");
            return ManagedConnection.wrap(connection, container, options);
        } catch (SQLException | RuntimeException | Error failure) {
            stopAfterFailure(container, options, failure);
            throw failure;
        } catch (Exception failure) {
            stopAfterFailure(container, options, failure);
            throw new SQLException("Failed to prepare PostgreSQL for jOOQ code generation", failure);
        }
    }

    private static void applyMigrations(PostgreSQLContainer<?> container, ConnectionOptions options) throws Exception {
        long startedAt = System.nanoTime();
        if (options.logs(Level.INFO)) {
            LOGGER.info("Applying Liquibase changelog: {}", options.liquibase().changeLog());
        }
        LiquibaseMigrationRunner.apply(container, options.liquibase());
        if (options.logs(Level.INFO)) {
            LOGGER.info("Liquibase migrations applied in {} ms", elapsedMillis(startedAt));
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
