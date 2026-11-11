package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;

/** Gaya dialog: header biru primer + badge, footer abu-abu dengan tombol di kanan (lihat styles.css). */
public final class DialogStyles {
    private DialogStyles() {
    }

    public static void apply(Dialog dialog, String title, String badge) {
        dialog.addClassName("app-dialog");
        dialog.setHeaderTitle(title);
        Span chip = new Span(badge);
        chip.getElement().getThemeList().add("badge");
        dialog.getHeader().add(chip);
        dialog.setCloseOnOutsideClick(false);
    }
}
