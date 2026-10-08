jooqTestcontainers {
    dependencies {
        migrationRuntime("example:extension:1.0")
    }
}

check(jooqTestcontainers.postgres.image.get() == "postgres:18")
check(jooqTestcontainers.postgres.startupTimeout.get().seconds == 60L)
check(jooqTestcontainers.liquibase.changeLog.get() == "db/changelog/db.changelog-master.yaml")
check(jooqTestcontainers.liquibase.searchPath.singleFile == file("src/main/resources"))
check(!jooqTestcontainers.liquibase.contexts.isPresent)
check(!jooqTestcontainers.liquibase.labels.isPresent)
check(jooqTestcontainers.liquibase.parameters.get().isEmpty())
check(jooqTestcontainers.logging.level.get().name == "INFO")
check(jooqTestcontainers.logging.enabled.get())
