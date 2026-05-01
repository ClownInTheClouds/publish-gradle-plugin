package dev.sorokin.publish.gradle.plugin;

import dev.sorokin.publish.gradle.plugin.entity.GradlePublishPluginExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public interface GradlePublishPlugin extends Plugin<Project> {

    void setupPluginExtension(Project project, GradlePublishPluginExtension extension);
}
