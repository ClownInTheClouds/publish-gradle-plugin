package dev.sorokin.gradle.publishplugin;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end {@code GradleRunner}-based tests exercising {@link GradlePublishPlugin}
 * against real consumer build scripts: default-coordinate publishing, sources jar
 * contributed via the component vs. created manually, and the BOM validation error.
 */
class GradlePublishPluginFunctionalTest {

    @TempDir
    Path projectDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'consumer'");
    }

    @Test
    void publishesLibraryWithDefaultCoordinates() throws IOException {
        writeBuildFile("""
                plugins {
                    id 'java'
                    id 'publish-plugin'
                }
                group = 'dev.sorokin.example'
                version = '1.0.0'
                publishing {
                    repositories {
                        maven { url = uri(layout.buildDirectory.dir('repo')) }
                    }
                }
                """);

        var result = runner("publish").build();

        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        var pom = projectDir.resolve("build/repo/dev/sorokin/example/consumer/1.0.0/consumer-1.0.0.pom");
        assertThat(pom).exists();
        assertThat(Files.readString(pom))
                .contains("<groupId>dev.sorokin.example</groupId>")
                .contains("<artifactId>consumer</artifactId>")
                .contains("<version>1.0.0</version>");
    }

    @Test
    void includesLazilyRegisteredSourcesJarAsArtifact() throws IOException {
        writeBuildFile("""
                plugins {
                    id 'java'
                    id 'publish-plugin'
                }
                group = 'dev.sorokin.example'
                version = '1.0.0'
                java { withSourcesJar() }
                publishing {
                    repositories {
                        maven { url = uri(layout.buildDirectory.dir('repo')) }
                    }
                }
                """);

        var result = runner("publish").build();

        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        var sourcesJar = projectDir.resolve("build/repo/dev/sorokin/example/consumer/1.0.0/consumer-1.0.0-sources.jar");
        assertThat(sourcesJar).exists();
    }

    @Test
    void includesManuallyCreatedSourcesJarNotCoveredByComponent() throws IOException {
        writeBuildFile("""
                plugins {
                    id 'java'
                    id 'publish-plugin'
                }
                group = 'dev.sorokin.example'
                version = '1.0.0'
                
                tasks.register('sourcesJar', Jar) {
                    archiveClassifier = 'sources'
                    from sourceSets.main.allJava
                }
                
                publishing {
                    repositories {
                        maven { url = uri(layout.buildDirectory.dir('repo')) }
                    }
                }
                """);

        var result = runner("publish").build();

        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        var sourcesJar = projectDir.resolve("build/repo/dev/sorokin/example/consumer/1.0.0/consumer-1.0.0-sources.jar");
        assertThat(sourcesJar).exists();
    }

    @Test
    void failsWithClearMessageWhenBomComponentIsMissing() throws IOException {
        writeBuildFile("""
                import dev.sorokin.gradle.publishplugin.PublicationType
                
                plugins {
                    id 'java'
                    id 'publish-plugin'
                }
                group = 'dev.sorokin.example'
                version = '1.0.0'
                publishPlugin {
                    publicationType = PublicationType.BOM
                }
                publishing {
                    repositories {
                        maven { url = uri(layout.buildDirectory.dir('repo')) }
                    }
                }
                """);

        var result = runner("publish").buildAndFail();

        assertThat(result.getOutput())
                .contains("requires the 'javaPlatform' component")
                .contains("java-platform");
    }

    private void writeBuildFile(String content) throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), content);
    }

    @SuppressWarnings("SameParameterValue")
    private GradleRunner runner(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(arguments)
                .withPluginClasspath();
    }
}