# publish-plugin

Gradle-плагин, который упрощает настройку Maven-публикации для Java-проектов.

Плагин автоматически конфигурирует `mavenJava` публикацию: group/artifact/version,
программный компонент (`java`, `javaPlatform`) и дополнительные артефакты
(`sourcesJar`, `javadocJar`), избегая дублирования при использовании
`java.withSourcesJar()` / `withJavadocJar()`.

## Возможности

- **Однострочная настройка** — применил плагин, и публикация уже сконфигурирована.
- **Разумные умолчания** — `groupId` и `version` берутся из проекта, `artifactId` — из имени проекта.
- **Поддержка компонентов** — публикуй библиотеки (`java`), Gradle-плагины (`java`) или BOM-файлы (`javaPlatform`).
- **Умные артефакты** — `sourcesJar` и `javadocJar` добавляются только если они ещё не были добавлены компонентом автоматически.
- **Понятные ошибки** — если выбранный тип публикации требует несуществующего компонента, плагин сообщает, какой плагин нужно применить.

## Требования

- Gradle 8.x+
- Java 17 (toolchain)

## Применение

```groovy
plugins {
    id 'java'
    id 'publish-plugin' version '2.0.0'
}
```

### Быстрый старт

Минимальная конфигурация — просто примени плагин. Всё остальное настроится автоматически:

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

После этого `gradle publish` опубликует артефакт с координатами
`com.example:<project-name>:1.0.0`.

## DSL-расширение `publishPlugin`

Все свойства являются `Property<T>` и могут быть переопределены:

| Свойство | Тип | По умолчанию | Описание |
|----------|-----|--------------|----------|
| `publicationGroup` | `Property<String>` | `project.group` | GroupId публикации |
| `publicationArtifactId` | `Property<String>` | `project.name` | ArtifactId публикации |
| `publicationVersion` | `Property<String>` | `project.version` | Версия публикации |
| `publicationType` | `Property<PublicationType>` | `PublicationType.LIBRARY` | Тип публикуемого компонента |

### Примеры конфигурации

**Переопределение координат:**

```groovy
publishPlugin {
    publicationGroup.set('io.github.example')
    publicationArtifactId.set('my-lib')
    publicationVersion.set('2.3.4')
}
```

**Публикация BOM-файла:**

```groovy
plugins {
    id 'java-platform'
    id 'publish-plugin' version '2.0.0'
}

publishPlugin {
    publicationType = dev.sorokin.gradle.publishplugin.PublicationType.BOM
}
```

## Типы публикаций

| Значение | Компонент | Требуемый плагин |
|----------|-----------|------------------|
| `PublicationType.LIBRARY` | `java` | `java` или `java-library` |
| `PublicationType.PLUGIN` | `java` | `java` или `java-library` |
| `PublicationType.BOM` | `javaPlatform` | `java-platform` |

> **Важно:** если выбранный компонент отсутствует в проекте, плагин выбросит
> `InvalidUserCodeException` с понятным сообщением о том, какой плагин нужно применить.

## Работа с `sourcesJar` и `javadocJar`

Плагин автоматически подхватывает задачи `sourcesJar` и `javadocJar`, если они
зарегистрированы в проекте. При этом он проверяет, не добавил ли уже компонент
(`java.withSourcesJar()` / `withJavadocJar()`) эти артефакты самостоятельно,
чтобы избежать дублирования и ошибок Gradle.

```groovy
plugins {
    id 'java'
    id 'publish-plugin' version '2.0.0'
}

java {
    withSourcesJar()   // будет опубликован автоматически
    withJavadocJar()   // будет опубликован автоматически
}
```

Или вручную:

```groovy
tasks.register('sourcesJar', Jar) {
    archiveClassifier = 'sources'
    from sourceSets.main.allJava
}
// publish-plugin подхватит этот артефакт
```

## Публикация плагина

Плагин сам является `java-gradle-plugin` и публикуется через `maven-publish`
(стандартный механизм Gradle Plugin Portal или собственный репозиторий).

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
