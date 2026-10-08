plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    id("ru.luttsev.jooq-testcontainers")
}

repositories {
    mavenCentral()
}

jooqTestcontainers {
    liquibase {
        changeLog.set("migrations/main.yaml")
        searchPath.setFrom("src/integration/resources", "src/integration/shared")
        contexts.set("development")
        labels.set("codegen")
        parameters.put("tableName", "configured_sample")
    }
}

jooq {
    configuration {
        generator {
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
            }
            target {
                packageName = "example.generated"
                directory = "build/generated-src/jooq"
            }
        }
    }
}
