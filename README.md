# jOOQ Testcontainers Gradle plugin

The plugin will prepare a temporary PostgreSQL database from migrations for the
official `org.jooq.jooq-codegen-gradle` plugin. Code generation configuration stays
in the standard `jooq` extension.

## Current scope

The foundation provides the DSL and runtime dependency configuration. Container
startup, Liquibase execution, JDBC integration and code generation inputs are not
implemented yet. Applying this version does not change `jooqCodegen` or create a
generation task.
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
`jooqTestcontainersRuntimeClasspath`. No runtime dependencies are bundled into
the Gradle plugin itself, and no repositories are added to the consuming project.

## Development

Build with the Gradle wrapper using JDK 17 or newer:

```shell
./gradlew check
```

The foundation targets Java 17 and is tested with Gradle 8.14.5. The eventual jOOQ
runtime can require a newer JDK. TestKit tests use a local Maven fixture to verify
the Kotlin DSL, version catalog providers, replacement of default declarations,
BOM resolution and configuration cache reuse for `help`. They do not require Docker.
