package ru.luttsev.jooq.testcontainers;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class JooqTestcontainersExtension {

    private final PostgresSettings postgres;
    private final LiquibaseSettings liquibase;
    private final LoggingSettings logging;
    private final RuntimeDependencies dependencies;

    @Inject
    public JooqTestcontainersExtension(ObjectFactory objects, RuntimeDependencies dependencies) {
        this.postgres = objects.newInstance(PostgresSettings.class);
        this.liquibase = objects.newInstance(LiquibaseSettings.class);
        this.logging = objects.newInstance(LoggingSettings.class);
        this.dependencies = dependencies;
    }

    public PostgresSettings getPostgres() { return postgres; }
    public LiquibaseSettings getLiquibase() { return liquibase; }
    public LoggingSettings getLogging() { return logging; }
    public RuntimeDependencies getDependencies() { return dependencies; }

    public void postgres(Action<? super PostgresSettings> action) { action.execute(postgres); }
    public void liquibase(Action<? super LiquibaseSettings> action) { action.execute(liquibase); }
    public void logging(Action<? super LoggingSettings> action) { action.execute(logging); }
    public void dependencies(Action<? super RuntimeDependencies> action) { action.execute(dependencies); }
}
