package dev.sorokin.gradle.publishplugin;

import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.jetbrains.annotations.NotNull;

public class GradlePublishPlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "publishPlugin";
    private static final String PUBLICATION_NAME = "mavenJava";
    private static final String SOURCES_JAR_TASK_NAME = "sourcesJar";
    private static final String JAVADOC_JAR_TASK_NAME = "javadocJar";

    @Override
    public void apply(@NotNull Project project) {
        project.getPluginManager().apply(MavenPublishPlugin.class);
        var extension = createExtension(project);
        project.afterEvaluate(p -> configurePublishing(p, extension));
    }

    private GradlePublishPluginExtension createExtension(Project project) {
        var extension = project.getExtensions().create(EXTENSION_NAME, GradlePublishPluginExtension.class, project.getObjects());
        extension.getPublicationGroup().convention(project.provider(() -> project.getGroup().toString()));
        extension.getPublicationArtifactId().convention(project.getName());
        extension.getPublicationVersion().convention(project.provider(() -> project.getVersion().toString()));
        return extension;
    }

    private void configurePublishing(Project project, GradlePublishPluginExtension extension) {
        project.getExtensions().configure(PublishingExtension.class, publishing -> {
            publishing.publications(publications -> publications.register(
                    PUBLICATION_NAME,
                    MavenPublication.class,
                    publication -> configurePublication(project, publication, extension)));
        });
    }

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

    private void addArtifactIfPresent(MavenPublication publication, Project project, String taskName) {
        var tasks = project.getTasks();
        if (tasks.getNames().contains(taskName)) {
            publication.artifact(tasks.named(taskName));
        }
    }
}
