package dev.sorokin.gradle.publishplugin;

import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * A Gradle plugin that simplifies configuring a project's Maven publication.
 *
 * <p>Applies {@link MavenPublishPlugin}, registers the {@code publishPlugin}
 * extension (see {@link GradlePublishPluginExtension}), and uses it to configure
 * a single {@code mavenJava} publication: the software component (based on
 * {@link PublicationType}), the group/artifactId/version coordinates, and,
 * if present, the {@code sourcesJar}/{@code javadocJar} tasks as artifacts.
 *
 * <p>This plugin does not configure publication repositories — use the
 * standard {@code publishing.repositories { ... }} block, which becomes
 * available automatically once this plugin is applied.
 *
 * <p>Example usage:
 * <pre>{@code
 * plugins {
 *     id 'java'
 *     id 'publish-plugin'
 * }
 *
 * publishPlugin {
 *     publicationType = PublicationType.LIBRARY
 * }
 *
 * publishing {
 *     repositories {
 *         maven { url = uri("https://my.repo/releases") }
 *     }
 * }
 * }</pre>
 *
 * @see GradlePublishPluginExtension
 * @see PublicationType
 */
public class GradlePublishPlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "publishPlugin";
    private static final String PUBLICATION_NAME = "mavenJava";
    private static final String SOURCES_JAR_TASK_NAME = "sourcesJar";
    private static final String JAVADOC_JAR_TASK_NAME = "javadocJar";
    private static final String JAR_EXTENSION = "jar";

    /**
     * Maps the well-known sources/javadoc jar task names to the Maven
     * classifier Gradle uses for the corresponding artifact when it's
     * contributed automatically via {@code java.withSourcesJar()} /
     * {@code withJavadocJar()}. Used to detect and skip artifacts that a
     * software component has already added to the publication, so we don't
     * add the same jar twice.
     */
    private static final Map<String, String> ARTIFACT_TASK_CLASSIFIERS = Map.of(
            SOURCES_JAR_TASK_NAME, "sources",
            JAVADOC_JAR_TASK_NAME, "javadoc"
    );

    /**
     * Applies {@link MavenPublishPlugin}, creates the {@code publishPlugin}
     * extension, and configures the {@code mavenJava} publication based on it.
     *
     * @param project the project this plugin is applied to
     */
    @Override
    public void apply(@NotNull Project project) {
        project.getPluginManager().apply(MavenPublishPlugin.class);
        var extension = createExtension(project);
        project.afterEvaluate(p -> configurePublishing(p, extension));
    }

    /**
     * Registers the {@link GradlePublishPluginExtension} with defaults:
     * group and version are taken from the {@link Project}, artifactId from
     * the project name.
     */
    private GradlePublishPluginExtension createExtension(Project project) {
        var extension = project.getExtensions().create(EXTENSION_NAME, GradlePublishPluginExtension.class, project.getObjects());
        extension.getPublicationGroup().convention(project.provider(() -> project.getGroup().toString()));
        extension.getPublicationArtifactId().convention(project.getName());
        extension.getPublicationVersion().convention(project.provider(() -> project.getVersion().toString()));
        return extension;
    }

    /**
     * Registers the {@code mavenJava} Maven publication.
     *
     * <p>Must be called from {@code project.afterEvaluate}, not eagerly during
     * {@link #apply}: the {@code maven-publish} plugin realizes registered
     * publications immediately (via an internal {@code withType(...).all { }}
     * listener) rather than lazily, so by the time {@link #configurePublication}
     * runs, the build script must have already finished executing — otherwise
     * project properties like {@code group} and {@code version}, if assigned
     * later in the script, would not yet be available.
     */
    private void configurePublishing(Project project, GradlePublishPluginExtension extension) {
        project.getExtensions().configure(PublishingExtension.class, publishing ->
                publishing.publications(publications -> publications.register(
                        PUBLICATION_NAME,
                        MavenPublication.class,
                        publication -> configurePublication(project, publication, extension))
                )
        );
    }

    /**
     * Populates the publication: component (based on {@link PublicationType}),
     * coordinates (group/artifactId/version), and, if present, the
     * sources/javadoc jar artifacts.
     *
     * @throws InvalidUserCodeException if the software component required by
     *         the selected {@link PublicationType} is missing from the project
     *         (for example, {@link PublicationType#BOM} is selected but the
     *         {@code java-platform} plugin is not applied)
     */
    private void configurePublication(Project project, MavenPublication publication, GradlePublishPluginExtension extension) {
        var type = extension.getPublicationType().get();
        var componentName = type.getComponentName();

        var components = project.getComponents();
        if (!components.getNames().contains(componentName)) {
            throw new InvalidUserCodeException(
                    "publishPlugin: publicationType '%s' requires the '%s' component, but it was not found. ".formatted(type, componentName) +
                            "Apply the 'java-platform' plugin for PublicationType.BOM; " +
                            "apply 'java' or 'java-library' for LIBRARY/PLUGIN."
            );
        }
        publication.from(components.named(componentName).get());
        publication.setGroupId(extension.getPublicationGroup().get());
        publication.setArtifactId(extension.getPublicationArtifactId().get());
        publication.setVersion(extension.getPublicationVersion().get());
        addArtifactIfPresent(publication, project, SOURCES_JAR_TASK_NAME);
        addArtifactIfPresent(publication, project, JAVADOC_JAR_TASK_NAME);
    }

    /**
     * Adds the {@code taskName} task to the publication as an artifact, if
     * such a task is registered in the project (including lazily, via
     * {@code tasks.register(...)}) and it was not already contributed by the
     * component added via {@code publication.from(component)} — which
     * happens automatically when the consumer uses
     * {@code java.withSourcesJar()} / {@code withJavadocJar()}. Without this
     * check, such consumers would end up with the same jar published twice,
     * which Gradle rejects.
     */
    private void addArtifactIfPresent(MavenPublication publication, Project project, String taskName) {
        var tasks = project.getTasks();
        if (!tasks.getNames().contains(taskName)) {
            return;
        }
        var classifier = ARTIFACT_TASK_CLASSIFIERS.get(taskName);
        Objects.requireNonNull(classifier, "no classifier mapping for task " + taskName);
        var alreadyPublished = publication.getArtifacts().stream()
                .anyMatch(artifact -> Objects.equals(classifier, artifact.getClassifier())
                        && JAR_EXTENSION.equals(artifact.getExtension()));
        if (!alreadyPublished) {
            publication.artifact(tasks.named(taskName));
        }
    }
}