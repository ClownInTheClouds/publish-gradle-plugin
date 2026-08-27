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
    LIBRARY("java"),
    /**
     * A Gradle plugin (the {@code java} component).
     */
    PLUGIN("java"),
    /**
     * A platform/BOM (the {@code javaPlatform} component, requires the {@code java-platform} plugin).
     */
    BOM("javaPlatform");

    private final String componentName;

    PublicationType(String componentName) {
        this.componentName = componentName;
    }

    /**
     * The name of the project's software component that will be published.
     */
    public String getComponentName() {
        return componentName;
    }
}