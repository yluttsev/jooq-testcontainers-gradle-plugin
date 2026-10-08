plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    id("ru.luttsev.jooq-testcontainers")
}

repositories {
    mavenCentral()
}

jooq {
    configuration {
        jdbc {
            initScript = "CREATE TABLE public.sample (id INTEGER PRIMARY KEY)"
        }
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
