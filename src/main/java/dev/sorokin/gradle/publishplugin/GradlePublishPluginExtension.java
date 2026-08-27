package dev.sorokin.gradle.publishplugin;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class GradlePublishPluginExtension {

    private final Property<String> publicationGroup;
    private final Property<String> publicationArtifactId;
    private final Property<String> publicationVersion;
    private final Property<PublicationType> publicationType;

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
}