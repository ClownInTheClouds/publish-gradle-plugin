package dev.sorokin.publish.gradle.plugin;

public enum PublicationType {

    LIBRARY("library", "java"),
    PLUGIN("plugin", "java"),
    BOM("bom", "javaPlatform");

    private final String publicationName;
    private final String componentName;

    PublicationType(String publicationName, String componentName) {
        this.publicationName = publicationName;
        this.componentName = componentName;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public String getComponentName() {
        return componentName;
    }
}