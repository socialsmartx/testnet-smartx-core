package com.smartx.message;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 This enum encapsulates available resource bundles of messages and an utility
 function getDefaultBundle for deciding the default locale

 <p>
 The locale used is the current value of the default locale for this instance
 of the Java Virtual Machine.
 </p>
 */
public enum ResourceBundles {
    //GUI_MESSAGES("com/smartx/gui/messages"),
    CLI_MESSAGES("com/smartx/cli/messages");
    private final String bundleName;
    ResourceBundles(String bundleName) {
        this.bundleName = bundleName;
    }
    public String getBundleName() {
        return bundleName;
    }
    public static ResourceBundle getDefaultBundle(ResourceBundles bundleName) {
        ResourceBundle defaultBundle = ResourceBundle.getBundle(bundleName.getBundleName(), Locale.ENGLISH);
        return defaultBundle == null ? ResourceBundle.getBundle(bundleName.getBundleName(), Locale.ENGLISH) : defaultBundle;
    }
    @Override
    public String toString() {
        return bundleName;
    }
}
