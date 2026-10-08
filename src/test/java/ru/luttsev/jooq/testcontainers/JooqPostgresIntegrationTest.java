package ru.luttsev.jooq.testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.luttsev.jooq.testcontainers.TestResources.readResource;

class JooqPostgresIntegrationTest {

    private static final String FIXTURE_DIRECTORY = "/fixtures/jooq-integration/";
    private static final String POSTGRES_IMAGE = "postgres:18";

    @TempDir
    Path project;

    @Test
    void configuresOfficialJooqTaskWithoutStartingDocker() throws IOException {
        writeBuild("jooq-integration.gradle.kts");
        run("verifyDriverClasspath");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JOOQ_TC_DOCKER_TEST", matches = "true")
    void generatesFromTemporaryPostgresAndClosesConnection() throws Exception {
        Set<String> before = runningPostgresContainers();
        writeBuild("jooq-codegen.gradle.kts");
        run("jooqCodegen", "--info");

        try (var files = Files.walk(project.resolve("build/generated-src/jooq"))) {
            assertTrue(files.anyMatch(file -> file.getFileName().toString().equals("Sample.java")));
        }
        assertTrue(run("jooqCodegen").getOutput().contains("UP-TO-DATE"));
        assertEquals(before, runningPostgresContainers());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JOOQ_TC_DOCKER_TEST", matches = "true")
    void stopsPostgresAfterCodegenFailure() throws Exception {
        Set<String> before = runningPostgresContainers();
        writeBuild("jooq-codegen.gradle.kts");
        Path build = project.resolve("build.gradle.kts");
        Files.writeString(build, Files.readString(build)
                .replace("org.jooq.meta.postgres.PostgresDatabase", "example.MissingDatabase"));

        runFailure("jooqCodegen", "--info");
        assertEquals(before, runningPostgresContainers());
    }

    private void writeBuild(String buildFile) throws IOException {
        Files.writeString(project.resolve("settings.gradle.kts"),
                readResource(FIXTURE_DIRECTORY + "settings.gradle.kts"));
        Files.writeString(project.resolve("build.gradle.kts"), readResource(FIXTURE_DIRECTORY + buildFile));
    }

    private BuildResult run(String... arguments) {
        return runner(arguments).build();
    }

    private BuildResult runFailure(String... arguments) {
        return runner(arguments).buildAndFail();
    }

    private GradleRunner runner(String... arguments) {
        return GradleRunner.create().withProjectDir(project.toFile()).withPluginClasspath()
                .withArguments(arguments);
    }

    private Set<String> runningPostgresContainers() throws IOException, InterruptedException {
        Process process = new ProcessBuilder("docker", "ps", "--filter", "ancestor=" + POSTGRES_IMAGE,
                "--format", "{{.ID}}").redirectErrorStream(true).start();
        Set<String> ids;
        try (var output = process.inputReader()) {
            ids = output.lines().filter(line -> !line.isBlank()).collect(Collectors.toSet());
        }
        if (process.waitFor() != 0) {
            throw new IOException("Could not inspect PostgreSQL containers: " + ids);
        }
        return ids;
    }
}
