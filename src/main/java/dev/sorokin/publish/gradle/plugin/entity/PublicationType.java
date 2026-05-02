package dev.sorokin.publish.gradle.plugin.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PublicationType {
    BOM("bom"),
    LIBRARY("library"),
    PLUGIN("plugin");

    @Getter
    private final String publicationName;
}
