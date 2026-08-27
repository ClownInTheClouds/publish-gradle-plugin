package dev.sorokin.gradle.publishplugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationTypeTest {

    @ParameterizedTest
    @EnumSource(PublicationType.class)
    void componentNameIsNeverBlank(PublicationType type) {
        assertThat(type.getComponentName()).isNotBlank();
    }

    @Test
    void libraryAndPluginShareTheJavaComponent() {
        assertThat(PublicationType.LIBRARY.getComponentName()).isEqualTo("java");
        assertThat(PublicationType.PLUGIN.getComponentName()).isEqualTo("java");
    }

    @Test
    void bomUsesTheJavaPlatformComponent() {
        assertThat(PublicationType.BOM.getComponentName()).isEqualTo("javaPlatform");
    }
}