package dev.sorokin.publish.gradle.plugin.exception;

public class ExtensionNotFoundException extends RuntimeException {
    public ExtensionNotFoundException(Class<?> type) {
        super("Extension " + type.getName() + " not found");
    }
}
