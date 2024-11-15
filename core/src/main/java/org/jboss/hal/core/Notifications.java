package org.jboss.hal.core;

import org.patternfly.component.alert.Alert;
import org.patternfly.component.alert.AlertDescription;
import org.patternfly.component.alert.AlertGroup;

import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.Severity.info;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.Severity.warning;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.alert.AlertGroup.toastAlertGroup;

/**
 * Static methods for creating notifications based {@link Alert} and added to {@link AlertGroup#toastAlertGroup()}
 */
public class Notifications {

    // ------------------------------------------------------ success

    public static void success(String message) {
        success(alert(success, message));
    }

    public static void success(String message, String details) {
        success(alert(success, message).addDescription(details));
    }

    public static void success(String message, AlertDescription details) {
        success(alert(success, message).addDescription(details));
    }

    public static void success(Alert alert) {
        notification(alert);
    }

    // ------------------------------------------------------ info

    public static void info(String message) {
        info(alert(info, message));
    }

    public static void info(String message, String details) {
        info(alert(info, message).addDescription(details));
    }

    public static void info(String message, AlertDescription details) {
        info(alert(info, message).addDescription(details));
    }

    public static void info(Alert alert) {
        notification(alert);
    }

    // ------------------------------------------------------ warning

    public static void warning(String message) {
        warning(alert(warning, message));
    }

    public static void warning(String message, String details) {
        warning(alert(warning, message).addDescription(details));
    }

    public static void warning(String message, AlertDescription details) {
        warning(alert(warning, message).addDescription(details));
    }

    public static void warning(Alert alert) {
        notification(alert);
    }

    // ------------------------------------------------------ error

    public static void error(String message) {
        error(alert(danger, message));
    }

    public static void error(String message, String details) {
        error(alert(danger, message).addDescription(details));
    }

    public static void error(String message, AlertDescription details) {
        error(alert(danger, message).addDescription(details));
    }

    public static void error(Alert alert) {
        notification(alert);
    }

    // ------------------------------------------------------ special

    public static void nyi() {
        info("Not yet implemented", "This feature is not yet implemented");
    }

    // ------------------------------------------------------ internal

    private static void notification(Alert alert) {
        toastAlertGroup().add(alert);
    }
}
