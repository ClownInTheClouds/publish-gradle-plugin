package dev.sorokin.gradle.publishplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class GradlePublishPlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "publishPlugin";

    @Override
    public void apply(@NotNull Project project) {
        project.getPluginManager().apply(MavenPublishPlugin.class);
        var extension = createExtension(project);
        configurePublishing(project, extension);
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
            publishing.repositories(repos -> extension.getRepositoryActions()
                    .forEach(action -> action.execute(repos)));
            publishing.publications(publications -> publications.register(
                    extension.getPublicationType().map(PublicationType::getPublicationName).get(),
                    MavenPublication.class,
                    publication -> configurePublication(project, publication, extension)));

            publishing.publications(publications -> publications.register(
                    "mavenJava",
                    MavenPublication.class,
                    publication -> configurePublication(project, publication, extension)));
        });
    }

    private void configurePublication(Project project, MavenPublication publication, GradlePublishPluginExtension extension) {
        publication.from(project.getComponents().named(extension.getPublicationType().get().getComponentName()).get());
        publication.setGroupId(extension.getPublicationGroup().get());
        publication.setArtifactId(extension.getPublicationArtifactId().get());
        publication.setVersion(extension.getPublicationVersion().get());
    }
}
