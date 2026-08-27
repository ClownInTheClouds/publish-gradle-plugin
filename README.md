# publish-plugin

[![EN](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![RU](https://img.shields.io/badge/lang-ru-blue.svg)](README.ru.md)

A Gradle plugin that simplifies Maven publication configuration for Java projects.

The plugin automatically configures the `mavenJava` publication: group/artifact/version,
software component (`java`, `javaPlatform`), and additional artifacts
(`sourcesJar`, `javadocJar`), avoiding duplication when using
`java.withSourcesJar()` / `withJavadocJar()`.

## Features

- **One-line setup** — apply the plugin, and the publication is already configured.
- **Sensible defaults** — `groupId` and `version` are taken from the project, `artifactId` from the project name.
- **Component support** — publish libraries (`java`), Gradle plugins (`java`), or BOM files (`javaPlatform`).
- **Smart artifacts** — `sourcesJar` and `javadocJar` are added only if they haven't already been added automatically by the component.
- **Clear errors** — if the selected publication type requires a missing component, the plugin tells you which plugin to apply.

## Requirements

- Gradle 8.x+
- Java 25 (toolchain)

## Usage

```groovy
plugins {
    id 'java'
    id 'publish-plugin' version '2.0.0'
}
```

### Quick Start

Minimal configuration — just apply the plugin. Everything else is set up automatically:

```groovy
plugins {
    id 'java'
    id 'publish-plugin' version '2.0.0'
}

group = 'com.example'
version = '1.0.0'

publishing {
    repositories {
        maven { url = uri('https://my.repo/maven2') }
    }
}
```

After that, `gradle publish` will publish the artifact with coordinates
`com.example:<project-name>:1.0.0`.

## DSL Extension `publishPlugin`

All properties are `Property<T>` and can be overridden:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `publicationGroup` | `Property<String>` | `project.group` | Publication groupId |
| `publicationArtifactId` | `Property<String>` | `project.name` | Publication artifactId |
| `publicationVersion` | `Property<String>` | `project.version` | Publication version |
| `publicationType` | `Property<PublicationType>` | `PublicationType.LIBRARY` | Type of published component |

### Configuration Examples

**Override coordinates:**

```groovy
publishPlugin {
    publicationGroup.set('io.github.example')
    publicationArtifactId.set('my-lib')
    publicationVersion.set('2.3.4')
}
```

**Publish a BOM file:**

```groovy
plugins {
    id 'java-platform'
    id 'publish-plugin' version '2.0.0'
}

publishPlugin {
    publicationType = dev.sorokin.gradle.publishplugin.PublicationType.BOM
}
```

## Publication Types

| Value | Component | Required Plugin |
|-------|-----------|---------------|
| `PublicationType.LIBRARY` | `java` | `java` or `java-library` |
| `PublicationType.PLUGIN` | `java` | `java` or `java-library` |
| `PublicationType.BOM` | `javaPlatform` | `java-platform` |

> **Important:** if the selected component is missing from the project, the plugin throws
> `InvalidUserCodeException` with a clear message about which plugin needs to be applied.

## Working with `sourcesJar` and `javadocJar`

The plugin automatically picks up `sourcesJar` and `javadocJar` tasks if they
are registered in the project. It also checks whether the component has already
added these artifacts itself (`java.withSourcesJar()` / `withJavadocJar()`),
to avoid duplication and Gradle errors.

```groovy
plugins {
    id 'java'
    id 'publish-plugin' version '2.0.0'
}

java {
    withSourcesJar()   // will be published automatically
    withJavadocJar()   // will be published automatically
}
```

Or manually:

```groovy
tasks.register('sourcesJar', Jar) {
    archiveClassifier = 'sources'
    from sourceSets.main.allJava
}
// publish-plugin will pick up this artifact
```

## Publishing the Plugin

The plugin itself is a `java-gradle-plugin` and is published via `maven-publish`
(standard Gradle Plugin Portal mechanism or a custom repository).

```groovy
publishing {
    repositories {
        maven {
            name = 'myRepo'
            url = uri('https://...')
            credentials {
                username = findProperty('repoUser')
                password = findProperty('repoPassword')
            }
        }
    }
}
```
