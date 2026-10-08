plugins {
    id("ru.luttsev.jooq-testcontainers")
}

repositories {
    maven { url = uri("repo") }
}

// TEST_CONFIGURATION

tasks.register("inspectRuntime") {
    inputs.files(configurations.named("jooqTestcontainersRuntimeClasspath"))
    doLast {
        inputs.files.files.sortedBy { it.name }.forEach { println("ARTIFACT=" + it.name) }
    }
}
