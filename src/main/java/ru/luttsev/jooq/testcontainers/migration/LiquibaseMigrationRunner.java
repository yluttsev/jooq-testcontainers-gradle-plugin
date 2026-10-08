package ru.luttsev.jooq.testcontainers.migration;

import java.sql.Connection;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.SearchPathResourceAccessor;
import org.testcontainers.containers.PostgreSQLContainer;

/** Applies a Liquibase changelog to a started PostgreSQL container. */
public final class LiquibaseMigrationRunner {

    private LiquibaseMigrationRunner() {
    }

    /**
     * Applies migrations before the code generator receives its database connection.
     *
     * @param container started PostgreSQL container
     * @param options changelog location and Liquibase filters
     * @throws Exception if resource loading, connection setup, or migration fails
     */
    public static void apply(PostgreSQLContainer<?> container, LiquibaseOptions options) throws Exception {
        try (Connection connection = container.createConnection("");
             SearchPathResourceAccessor resources = resourceAccessor(options)) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (database) {
                update(database, resources, options);
            }
        }
    }

    private static SearchPathResourceAccessor resourceAccessor(LiquibaseOptions options) throws Exception {
        SearchPathResourceAccessor resources = new SearchPathResourceAccessor("");
        try {
            for (var path : options.searchPaths()) {
                resources.addResourceAccessor(path.toUri().toString());
            }
            return resources;
        } catch (Exception failure) {
            try {
                resources.close();
            } catch (Exception cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private static void update(Database database,
                               SearchPathResourceAccessor resources,
                               LiquibaseOptions options) throws LiquibaseException {
        Liquibase liquibase = new Liquibase(options.changeLog(), resources, database);
        options.parameters().forEach(liquibase::setChangeLogParameter);
        liquibase.update(new Contexts(options.contexts()), new LabelExpression(options.labels()));
    }
}
