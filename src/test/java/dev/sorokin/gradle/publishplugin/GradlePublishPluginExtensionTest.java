package dev.sorokin.gradle.publishplugin;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fast, {@code ProjectBuilder}-based tests for the default conventions
 * exposed by {@link GradlePublishPluginExtension} (group/artifactId/version/type)
 * and their overridability.
 */
class GradlePublishPluginExtensionTest {

    private org.gradle.api.Project project;
    private GradlePublishPluginExtension extension;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        project.setGroup("dev.sorokin.example");
        project.setVersion("1.2.3");
        project.getPluginManager().apply("java");
        project.getPluginManager().apply(GradlePublishPlugin.class);
        extension = project.getExtensions().getByType(GradlePublishPluginExtension.class);
    }

    @Test
    void publicationTypeDefaultsToLibrary() {
        assertThat(extension.getPublicationType().get()).isEqualTo(PublicationType.LIBRARY);
    }

    @Test
    void groupDefaultsToProjectGroup() {
        assertThat(extension.getPublicationGroup().get()).isEqualTo("dev.sorokin.example");
    }

    @Test
    void artifactIdDefaultsToProjectName() {
        assertThat(extension.getPublicationArtifactId().get()).isEqualTo(project.getName());
    }

    @Test
    void versionDefaultsToProjectVersion() {
        assertThat(extension.getPublicationVersion().get()).isEqualTo("1.2.3");
    }

    @Test
    void conventionsCanBeOverridden() {
        extension.getPublicationGroup().set("overridden.group");
        extension.getPublicationArtifactId().set("overridden-artifact");
        extension.getPublicationVersion().set("9.9.9");
        extension.getPublicationType().set(PublicationType.PLUGIN);

        assertThat(extension.getPublicationGroup().get()).isEqualTo("overridden.group");
        assertThat(extension.getPublicationArtifactId().get()).isEqualTo("overridden-artifact");
        assertThat(extension.getPublicationVersion().get()).isEqualTo("9.9.9");
        assertThat(extension.getPublicationType().get()).isEqualTo(PublicationType.PLUGIN);
    }
}