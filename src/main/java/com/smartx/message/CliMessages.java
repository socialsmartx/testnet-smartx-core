package com.smartx.message;

import java.util.ResourceBundle;

public final class CliMessages {
    private static final ResourceBundle RESOURCES = ResourceBundles.getDefaultBundle(ResourceBundles.CLI_MESSAGES);
    private CliMessages() {
    }
    public static String get(String key, Object... args) {
        return MessageFormatter.get(RESOURCES, key, args);
    }
}
