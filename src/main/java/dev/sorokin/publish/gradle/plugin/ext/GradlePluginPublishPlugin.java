package dev.sorokin.publish.gradle.plugin.ext;

import dev.sorokin.publish.gradle.plugin.entity.PublicationType;
import dev.sorokin.publish.gradle.plugin.impl.DefaultGradlePublishPlugin;
import dev.sorokin.publish.gradle.plugin.entity.GradlePublishPluginExtension;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

public class GradlePluginPublishPlugin extends DefaultGradlePublishPlugin {

    @Override
    public void setupPluginExtension(Project project, GradlePublishPluginExtension extension) {
        extension.setPublicationName(PublicationType.PLUGIN.getPublicationName());
        extension.setBuildComponent(project.getComponents().getByName("java"));
        extension.setPublicationGroup(project.getGroup().toString());
        extension.setPublicationArtifactId(project.getName());
        extension.setPublicationVersion(project.getVersion().toString());
        //TODO Add other repositories as needed
        extension.setPublicationRepositories(RepositoryHandler::mavenLocal);
    }
}
