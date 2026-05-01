package dev.sorokin.publish.gradle.plugin.entity;

import lombok.Data;
import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.component.SoftwareComponent;

@Data
public class GradlePublishPluginExtension {
    private String publicationName;
    private SoftwareComponent buildComponent;
    private String publicationGroup;
    private String publicationArtifactId;
    private String publicationVersion;
    private Action<RepositoryHandler> publicationRepositories;
}
