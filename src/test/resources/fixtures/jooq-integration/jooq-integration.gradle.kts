import org.jooq.codegen.gradle.CodegenPluginExtension
import org.jooq.codegen.gradle.CodegenTask
import java.net.URLClassLoader

plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    id("ru.luttsev.jooq-testcontainers")
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
check(jdbc.driver == "ru.luttsev.jooq.testcontainers.jdbc.PostgresContainerDriver")
check(jdbc.url == "jdbc:jooq-testcontainers:postgresql:///codegen?image=postgres%3A18-alpine&startupTimeoutSeconds=60&logLevel=INFO&loggingEnabled=true")
check(jooqExtension.executions.getByName("").configuration.generator.target.packageName == "example.generated")
check(configurations.getByName("jooqCodegen").extendsFrom.any {
    it.name == "jooqTestcontainersRuntimeClasspath"
})
tasks.register("verifyDriverClasspath") {
    val codegen = tasks.named<CodegenTask>("jooqCodegen")
    inputs.files(codegen.map { it.classpath })
    doLast {
        val files = inputs.files.files
        check(files.any { it.name.startsWith("testcontainers-postgresql-") })
        check(files.any { it.name.startsWith("postgresql-") })
        URLClassLoader(files.map { it.toURI().toURL() }.toTypedArray(),
            CodegenTask::class.java.classLoader).use { loader ->
            check(loader.loadClass("ru.luttsev.jooq.testcontainers.jdbc.PostgresContainerDriver") != null)
        }
    }
}
