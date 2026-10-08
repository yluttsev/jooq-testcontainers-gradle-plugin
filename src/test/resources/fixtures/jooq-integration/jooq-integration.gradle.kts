import org.jooq.codegen.gradle.CodegenPluginExtension
import java.net.URLClassLoader

plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    id("io.github.yluttsev.jooq-testcontainers")
}

repositories {
    mavenCentral()
}

jooqTestcontainers {
    postgres {
        image.set("postgres:18-alpine")
    }
}

jooq {
    configuration {
        generator {
            target {
                packageName = "example.generated"
            }
        }
    }
}

tasks.named("jooqCodegen").get()
val jooqExtension = extensions.getByType<CodegenPluginExtension>()
val jdbc = jooqExtension.executions.getByName("").configuration.jdbc
check(jdbc.driver == "io.github.yluttsev.jooq.testcontainers.jdbc.PostgresContainerDriver")
check(jdbc.url.startsWith("jdbc:jooq-testcontainers:postgresql:///codegen?image=postgres%3A18-alpine"))
check(jdbc.url.contains("changeLog=db%2Fchangelog%2Fdb.changelog-master.yaml"))
check(jooqExtension.executions.getByName("").configuration.generator.target.packageName == "example.generated")
check(configurations.getByName("jooqCodegen").extendsFrom.any {
    it.name == "jooqTestcontainersRuntimeClasspath"
})
tasks.register("verifyDriverClasspath") {
    inputs.files(configurations.named("jooqCodegen"))
    doLast {
        val files = inputs.files.files
        check(files.any { it.name.startsWith("testcontainers-postgresql-") })
        check(files.any { it.name.startsWith("postgresql-") })
        URLClassLoader(files.map { it.toURI().toURL() }.toTypedArray(),
            CodegenPluginExtension::class.java.classLoader).use { loader ->
            check(loader.loadClass("io.github.yluttsev.jooq.testcontainers.jdbc.PostgresContainerDriver") != null)
        }
    }
}
