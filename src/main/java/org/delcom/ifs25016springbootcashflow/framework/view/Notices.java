package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** Notifikasi seragam untuk sukses dan gagal. */
final class Notices {
    private Notices() {
    }

    static void success(String message) {
        Notification.show(message, 3500, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    static void error(String message) {
        Notification.show(message, 5000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
