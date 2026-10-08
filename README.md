# jOOQ Testcontainers Gradle plugin

The plugin prepares a temporary PostgreSQL database for the
official `org.jooq.jooq-codegen-gradle` plugin. Code generation configuration stays
in the standard `jooq` extension.

## Current scope

When the official jOOQ plugin is applied, the plugin configures its default
`jooqCodegen` task with a JDBC driver that starts PostgreSQL at connection time.
The driver closes the container when jOOQ closes the connection, including after
generation errors. It also stops partially started containers if connection setup
fails. No extra generation task is created. Liquibase migrations and named jOOQ
executions are not integrated yet.
The plugin sets the JDBC driver and URL in the standard jOOQ configuration;
generator settings such as target package, schema, and forced types remain there.
It applies `java-base` to enable Gradle's JVM dependency and Maven BOM handling;
it does not create Java source sets.

## Configuration

```kotlin
import org.slf4j.event.Level
import java.time.Duration

plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    id("ru.luttsev.jooq-testcontainers") version "1.0-SNAPSHOT"
}

repositories {
    mavenCentral()
}

jooq {
    configuration {
        generator {
            // Standard jOOQ configuration
        }
    }
}

jooqTestcontainers {
    postgres {
        image.set("postgres:18")
        startupTimeout.set(Duration.ofSeconds(60))
    }
    liquibase {
        changeLog.set("db/changelog/db.changelog-master.yaml")
        // searchPath already contains src/main/resources.
        // Use setFrom(...) to replace the roots, from(...) to add roots.
        searchPath.setFrom("src/main/resources")
        contexts.set("codegen") // Optional; absent by default
        labels.set("community") // Optional; absent by default
        parameters.put("schemaName", "public") // Empty by default
    }
    logging {
        level.set(Level.INFO) // Only this plugin's messages
        enabled.set(true) // Set false to disable this plugin's messages
    }
    dependencies {
        // Optional overrides from a user's version catalog:
        liquibase(libs.liquibase.core)
        postgresDriver(libs.postgresql)
        testcontainersPlatform(libs.testcontainers.bom)
        migrationRuntime(libs.liquibase.extension)
    }
}
```

All configuration is optional. Changelog paths are relative to the search roots.
Filters follow Liquibase's standard behavior; absence of a filter does not imply
that tagged changesets are excluded. The plugin does not exclude Liquibase tables
from jOOQ generation automatically.

Dependency methods also accept `group:artifact:version` strings.
`liquibase`, `postgresDriver` and `testcontainersPlatform` replace their respective
default declarations; repeated calls use the last declaration. `migrationRuntime`
adds dependencies without removing any defaults. Gradle still applies normal
transitive dependency conflict resolution.

Initial defaults:

| Dependency | Version |
| --- | --- |
| Liquibase core | 4.33.0 |
| PostgreSQL JDBC | 42.7.13 |
| Testcontainers BOM | 2.0.5 |

The BOM supplies versions for the `testcontainers` and
`testcontainers-postgresql` modules (Testcontainers 2.x artifact names).
Overrides must be compatible with these modules and, once implemented, the
migration adapter. End-to-end compatibility is not established by foundation tests.

`jooqMigrationRuntime` is also available for declarations in the standard
`dependencies` block. The internal resolvable configuration is
`jooqTestcontainersRuntimeClasspath`. The plugin adds its own jar to jOOQ's
codegen classpath so the generator can load the JDBC driver. Testcontainers and
the PostgreSQL driver are resolved from the consuming project's repositories.
No repositories are added to the consuming project.

## Development

Build with the Gradle wrapper using JDK 21 or newer:

```shell
./gradlew check
```

The plugin targets Java 21 and is tested with Gradle 8.14.5. jOOQ 3.21.9 also
requires Java 21. TestKit tests use a local Maven fixture to verify
the Kotlin DSL, version catalog providers, replacement of default declarations,
BOM resolution and configuration cache reuse for `help`. The jOOQ integration
test checks the official plugin configuration and driver classpath without Docker.
Set `JOOQ_TC_DOCKER_TEST=true` when running `test` to also execute jOOQ generation
against a real `postgres:18` container and check cleanup after success and failure.
