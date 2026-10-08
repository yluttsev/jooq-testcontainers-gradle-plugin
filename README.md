# jOOQ Testcontainers Gradle plugin

Runs the official jOOQ code generator against a temporary PostgreSQL container.
Liquibase applies migrations before jOOQ connects. The container is removed after
code generation or a failure. Configure generation in the official `jooq` block.

## Minimal setup

Requires Java 21 and a running Docker daemon. Add both plugins to `build.gradle.kts`:

```kotlin
plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    id("ru.luttsev.jooq-testcontainers") version "0.1.0"
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
```

Create `src/main/resources/db/changelog/db.changelog-master.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: create-sample
      author: example
      changes:
        - createTable:
            tableName: sample
            columns:
              - column:
                  name: id
                  type: int
                  constraints:
                    primaryKey: true
```

Run `./gradlew jooqCodegen`. The plugin uses `postgres:18` by default. No extra
code generation task is created.

## Options

Set only the values you need in `jooqTestcontainers { ... }`:

| Block | Property | Default | Purpose |
| --- | --- | --- | --- |
| `postgres` | `image` | `postgres:18` | PostgreSQL Docker image |
| `postgres` | `startupTimeout` | 60 seconds | Container startup limit (`Duration`) |
| `liquibase` | `changeLog` | `db/changelog/db.changelog-master.yaml` | Changelog path relative to a search root |
| `liquibase` | `searchPath` | `src/main/resources` | Roots for the changelog and included files |
| `liquibase` | `contexts` | unset | Liquibase context filter |
| `liquibase` | `labels` | unset | Liquibase label filter |
| `liquibase` | `parameters` | empty | Changelog parameters |
| `logging` | `level` | `Level.INFO` | Minimum level for this plugin's messages |
| `logging` | `enabled` | `true` | Enable this plugin's messages |

For example:

```kotlin
import org.slf4j.event.Level
import java.time.Duration

jooqTestcontainers {
    postgres {
        image.set("postgres:18-alpine")
        startupTimeout.set(Duration.ofSeconds(90))
    }
    liquibase {
        changeLog.set("migrations/main.yaml")
        searchPath.setFrom("src/integration/resources")
        contexts.set("codegen")
        labels.set("generated")
        parameters.put("schemaName", "public")
    }
    logging {
        level.set(Level.DEBUG)
    }
}
```

`searchPath.setFrom(...)` replaces the default root; `searchPath.from(...)` adds
roots. Gradle tracks their contents as inputs of `jooqCodegen`.

## Runtime dependencies

The plugin declares Liquibase 4.33.0, PostgreSQL JDBC 42.7.13, and the
Testcontainers 2.0.5 BOM by default. Override them with dependency notation or
version catalog providers. For a project with a version catalog:

```kotlin
jooqTestcontainers {
    dependencies {
        liquibase(libs.liquibase.core)
        postgresDriver(libs.postgresql)
        testcontainersPlatform(libs.testcontainers.bom)
        migrationRuntime(libs.liquibase.extension)
    }
}
```

The first three methods replace their defaults. `migrationRuntime` adds libraries
needed by migrations; it can be called more than once. The consuming project must
declare repositories for these dependencies.

The tested combination is Gradle 8.14.5, Java 21, jOOQ 3.21.9, the default
runtime dependencies above, and PostgreSQL 18. The plugin currently integrates
Liquibase and the default `jooqCodegen` execution.
