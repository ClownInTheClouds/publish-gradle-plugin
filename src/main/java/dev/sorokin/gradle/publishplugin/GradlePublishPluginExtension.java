package dev.sorokin.gradle.publishplugin;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * The {@code publishPlugin} DSL extension used to configure the Maven
 * publication created by {@link GradlePublishPlugin}.
 *
 * <p>Defaults for group/artifactId/version are set by the plugin
 * ({@link GradlePublishPlugin#apply}) based on the {@link org.gradle.api.Project},
 * not in this class's constructor — override them from the build script if
 * non-default values are needed.
 */
public abstract class GradlePublishPluginExtension {

    private final Property<String> publicationGroup;
    private final Property<String> publicationVersion;
    private final Property<String> publicationArtifactId;
    private final Property<PublicationType> publicationType;

    /**
     * Constructor for the extension, using the given object factory.
     *
     * @param objects the object factory for creating properties
     */
    @Inject
    public GradlePublishPluginExtension(ObjectFactory objects) {
        this.publicationGroup = objects.property(String.class);
        this.publicationVersion = objects.property(String.class);
        this.publicationArtifactId = objects.property(String.class);
        this.publicationType = objects.property(PublicationType.class).convention(PublicationType.LIBRARY);
    }

    /**
     * The publication's groupId. Defaults to {@code project.getGroup()}.
     *
     * @return the group ID property
     */
    public Property<String> getPublicationGroup() {
        return publicationGroup;
    }

    /**
     * The publication's artifactId. Defaults to the project name.
     *
     * @return the artifact ID property
     */
    public Property<String> getPublicationArtifactId() {
        return publicationArtifactId;
    }

    /**
     * The publication's version. Defaults to {@code project.getVersion()}.
     *
     * @return the version property
     */
    public Property<String> getPublicationVersion() {
        return publicationVersion;
    }

    /**
     * The type of the published component. Defaults to
     * {@link PublicationType#LIBRARY}. Determines which software component
     * of the project is published (see {@link PublicationType#getComponentName()});
     * the corresponding component must exist in the project by the time the
     * publication is realized.
     *
     * @return the publication type property
     */
    public Property<PublicationType> getPublicationType() {
        return publicationType;
    }
}