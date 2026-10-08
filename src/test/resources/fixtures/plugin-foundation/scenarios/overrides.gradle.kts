jooqTestcontainers {
    postgres {
        image.set("postgres:17")
    }
    liquibase {
        changeLog.set("master.xml")
        searchPath.setFrom("migrations")
        contexts.set("codegen")
        labels.set("community")
        parameters.put("schemaName", "public")
    }
    logging {
        level.set(org.slf4j.event.Level.DEBUG)
        enabled.set(false)
    }
    dependencies {
        liquibase("org.liquibase:liquibase-core:4.32.0")
        postgresDriver("org.postgresql:postgresql:42.7.8")
        testcontainersPlatform("org.testcontainers:testcontainers-bom:2.0.4")
    }
}

check(jooqTestcontainers.postgres.image.get() == "postgres:17")
check(jooqTestcontainers.liquibase.searchPath.singleFile == file("migrations"))
check(jooqTestcontainers.liquibase.contexts.get() == "codegen")
check(jooqTestcontainers.liquibase.labels.get() == "community")
check(jooqTestcontainers.liquibase.parameters.get()["schemaName"] == "public")
check(jooqTestcontainers.logging.level.get().name == "DEBUG")
check(!jooqTestcontainers.logging.enabled.get())
