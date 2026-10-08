plugins {
    `java-gradle-plugin`
}

group = "ru.luttsev"
version = "1.0-SNAPSHOT"

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
    plugins {
        create("jooqTestcontainers") {
            id = "ru.luttsev.jooq-testcontainers"
            implementationClass = "ru.luttsev.jooq.testcontainers.JooqTestcontainersPlugin"
            displayName = "jOOQ Testcontainers"
            description = "Prepares a temporary PostgreSQL database for jOOQ code generation."
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
