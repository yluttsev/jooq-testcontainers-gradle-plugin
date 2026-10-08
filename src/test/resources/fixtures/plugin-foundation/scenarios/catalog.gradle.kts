jooqTestcontainers {
    dependencies {
        liquibase(libs.liquibase.core)
        postgresDriver(libs.postgresql)
        testcontainersPlatform(libs.testcontainers.bom)
        migrationRuntime(libs.liquibase.extension)
    }
}
