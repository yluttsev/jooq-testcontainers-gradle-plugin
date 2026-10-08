import org.gradle.plugin.compatibility.compatibility

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.2.1"
}

group = "io.github.yluttsev"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.testcontainers:testcontainers-postgresql:2.0.5")
    compileOnly("org.liquibase:liquibase-core:4.33.0")
    testImplementation(gradleTestKit())
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

gradlePlugin {
    website.set("https://github.com/yluttsev/jooq-testcontainers-gradle-plugin")
    vcsUrl.set("https://github.com/yluttsev/jooq-testcontainers-gradle-plugin")

    plugins {
        create("jooqTestcontainers") {
            id = "io.github.yluttsev.jooq-testcontainers"
            implementationClass = "io.github.yluttsev.jooq.testcontainers.JooqTestcontainersPlugin"
            displayName = "jOOQ Testcontainers"
            description = "Prepares a temporary PostgreSQL database for jOOQ code generation."
            tags.set(listOf("jooq", "testcontainers", "postgresql", "liquibase", "codegen"))

            compatibility {
                features {
                    configurationCache = false
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
