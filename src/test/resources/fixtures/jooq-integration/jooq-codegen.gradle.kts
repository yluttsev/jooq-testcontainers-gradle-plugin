plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    id("io.github.yluttsev.jooq-testcontainers")
}

repositories {
    mavenCentral()
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
