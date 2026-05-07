package dev.sorokin.publish.gradle.plugin;

import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class GradlePublishPluginExtension {

    private final Property<String> publicationGroup;
    private final Property<String> publicationArtifactId;
    private final Property<String> publicationVersion;
    private final Property<PublicationType> publicationType;

    private final List<Action<? super RepositoryHandler>> repositoryActions = new ArrayList<>();

    @Inject
    public GradlePublishPluginExtension(ObjectFactory objects) {
        this.publicationGroup = objects.property(String.class).convention("");
        this.publicationArtifactId = objects.property(String.class);
        this.publicationVersion = objects.property(String.class).convention("");
        this.publicationType = objects.property(PublicationType.class).convention(PublicationType.LIBRARY);
    }

    public Property<String> getPublicationGroup() {
        return publicationGroup;
    }

    public Property<String> getPublicationArtifactId() {
        return publicationArtifactId;
    }

    public Property<String> getPublicationVersion() {
        return publicationVersion;
    }

    public Property<PublicationType> getPublicationType() {
        return publicationType;
    }

    public List<Action<? super RepositoryHandler>> getRepositoryActions() {
        return repositoryActions;
    }

    @SuppressWarnings("unused")
    public void repositories(Action<? super RepositoryHandler> action) {
        repositoryActions.add(action);
    }
}