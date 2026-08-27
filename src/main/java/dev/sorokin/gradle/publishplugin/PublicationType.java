package dev.sorokin.gradle.publishplugin;

/**
 * The type of artifact published by {@link GradlePublishPlugin}.
 *
 * <p>Each value maps to the name of a software component
 * ({@link #getComponentName()}) that must exist in the project by the time
 * the publication is realized — otherwise the plugin throws an
 * {@link org.gradle.api.InvalidUserCodeException} indicating which plugin
 * needs to be applied. The component becomes available once the
 * corresponding plugin is applied:
 * <ul>
 *   <li>{@link #LIBRARY}, {@link #PLUGIN} — require {@code java} or {@code java-library}</li>
 *   <li>{@link #BOM} — requires {@code java-platform}</li>
 * </ul>
 */
public enum PublicationType {

    /**
     * A regular Java library (the {@code java} component).
     */
    LIBRARY("library", "java"),
    /**
     * A Gradle plugin (the {@code java} component).
     */
    PLUGIN("plugin", "java"),
    /**
     * A platform/BOM (the {@code javaPlatform} component, requires the {@code java-platform} plugin).
     */
    BOM("bom", "javaPlatform");

    private final String publicationName;
    private final String componentName;

    PublicationType(String publicationName, String componentName) {
        this.publicationName = publicationName;
        this.componentName = componentName;
    }

    /**
     * The conventional publication name (currently unused, reserved).
     */
    @SuppressWarnings("unused")
    public String getPublicationName() {
        return publicationName;
    }

    /**
     * The name of the project's software component that will be published.
     */
    public String getComponentName() {
        return componentName;
    }
}