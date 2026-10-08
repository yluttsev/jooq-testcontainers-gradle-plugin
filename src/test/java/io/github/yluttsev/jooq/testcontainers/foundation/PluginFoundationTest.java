package io.github.yluttsev.jooq.testcontainers.foundation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.github.yluttsev.jooq.testcontainers.support.TestResources.readResource;

class PluginFoundationTest {

    @TempDir
    Path project;

    @Test
    void defaultsResolveAndAddingExtensionPreservesThem() throws IOException {
        prepareRepository();
        writeBuild("defaults");

        String output = run("inspectRuntime").getOutput();
        assertTrue(output.contains("ARTIFACT=liquibase-core-4.33.0.jar"));
        assertTrue(output.contains("ARTIFACT=postgresql-42.7.13.jar"));
        assertTrue(output.contains("ARTIFACT=testcontainers-postgresql-2.0.5.jar"));
        assertTrue(output.contains("ARTIFACT=extension-1.0.jar"));
    }

    @Test
    void stringOverridesReplaceDefaultsIncludingLowerVersions() throws IOException {
        prepareRepository();
        writeBuild("overrides");

        assertOverrides(run("inspectRuntime").getOutput());
    }

    @Test
    void versionCatalogProvidersWorkAndConfigurationCacheIsReusable() throws IOException {
        prepareRepository();
        Files.createDirectories(project.resolve("gradle"));
        Files.writeString(project.resolve("gradle/libs.versions.toml"), resource("libs.versions.toml"));
        writeBuild("catalog");

        assertOverrides(run("inspectRuntime").getOutput());
        run("help", "--configuration-cache");
        assertTrue(run("help", "--configuration-cache").getOutput().contains("Reusing configuration cache."));
    }

    private void assertOverrides(String output) {
        assertTrue(output.contains("ARTIFACT=liquibase-core-4.32.0.jar"));
        assertTrue(output.contains("ARTIFACT=postgresql-42.7.8.jar"));
        assertTrue(output.contains("ARTIFACT=testcontainers-postgresql-2.0.4.jar"));
        assertFalse(output.contains("ARTIFACT=liquibase-core-4.33.0.jar"));
        assertFalse(output.contains("ARTIFACT=postgresql-42.7.13.jar"));
        assertFalse(output.contains("ARTIFACT=testcontainers-postgresql-2.0.5.jar"));
    }

    private void writeBuild(String scenario) throws IOException {
        Files.writeString(project.resolve("settings.gradle.kts"), resource("settings.gradle.kts"));
        String build = resource("build.gradle.kts")
                .replace("// TEST_CONFIGURATION", resource("scenarios/" + scenario + ".gradle.kts"));
        Files.writeString(project.resolve("build.gradle.kts"), build);
    }

    private String resource(String name) throws IOException {
        return readResource("/fixtures/plugin-foundation/" + name);
    }

    private BuildResult run(String... arguments) {
        String[] options = new String[arguments.length + 3];
        System.arraycopy(arguments, 0, options, 0, arguments.length);
        options[arguments.length] = "--offline";
        options[arguments.length + 1] = "--stacktrace";
        options[arguments.length + 2] = "--console=plain";
        return GradleRunner.create().withProjectDir(project.toFile()).withPluginClasspath()
                .withArguments(options).build();
    }

    private void prepareRepository() throws IOException {
        module("org.liquibase", "liquibase-core", "4.33.0");
        module("org.liquibase", "liquibase-core", "4.32.0");
        module("org.postgresql", "postgresql", "42.7.13");
        module("org.postgresql", "postgresql", "42.7.8");
        module("example", "extension", "1.0");
        for (String version : new String[]{"2.0.5", "2.0.4"}) {
            module("org.testcontainers", "testcontainers", version);
            module("org.testcontainers", "testcontainers-postgresql", version);
            String managed = resource("maven/bom-content.xml").formatted(version, version);
            pom("org.testcontainers", "testcontainers-bom", version, managed);
        }
    }

    private void module(String group, String artifact, String version) throws IOException {
        Path directory = pom(group, artifact, version, "");
        try (JarOutputStream ignored = new JarOutputStream(
                Files.newOutputStream(directory.resolve(artifact + "-" + version + ".jar")))) {
        }
    }

    private Path pom(String group, String artifact, String version, String extra) throws IOException {
        Path directory = project.resolve("repo/" + group.replace('.', '/') + "/" + artifact + "/" + version);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(artifact + "-" + version + ".pom"),
                resource("maven/pom.xml").formatted(group, artifact, version, extra));
        return directory;
    }
}
