package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

/** Modal pratinjau gambar: klik gambar untuk zoom 1.0x / 1.8x, atau pakai tombol Perkecil / Perbesar. */
public class ImagePreviewDialog extends Dialog {
    private static final double MIN = 0.6;
    private static final double MAX = 3.0;
    private static final double STEP = 0.4;
    private static final double CLICK_ZOOM = 1.8;
    private final Image image;
    private double scale = 1.0;

    public ImagePreviewDialog(String url, String title) {
        DialogStyles.apply(this, title, "Pratinjau");
        setWidth("min(900px, 95vw)");
        image = new Image(url, title);
        image.getStyle().set("max-width", "none").set("display", "block");
        image.addClickListener(e -> {
            scale = scale > 1.0 ? 1.0 : CLICK_ZOOM;
            applyScale();
        });
        applyScale();
        Div box = new Div(image);
        box.addClassName("preview-box");
        add(box);

        Button smaller = new Button("Perkecil", VaadinIcon.MINUS.create(), e -> {
            scale = Math.max(MIN, scale - STEP);
            applyScale();
        });
        Button bigger = new Button("Perbesar", VaadinIcon.PLUS.create(), e -> {
            scale = Math.min(MAX, scale + STEP);
            applyScale();
        });
        Button close = new Button("Tutup", VaadinIcon.CLOSE.create(), e -> close());
        close.addClassName("btn-accent");
        getFooter().add(smaller, bigger, close);
    }

    private void applyScale() {
        image.setWidth(scale * 100 + "%");
        image.getStyle().set("cursor", scale > 1.0 ? "zoom-out" : "zoom-in");
    }

    /** Thumbnail bukti yang bisa diklik; "-" bila tidak ada bukti. */
    public static Component thumbnail(String proofPath, String altText) {
        if (proofPath == null || proofPath.isBlank()) {
            return new Span("-");
        }
        String url = "/uploads/" + proofPath;
        Image thumb = new Image(url, altText);
        thumb.addClassName("proof-thumb");
        thumb.addClickListener(e -> new ImagePreviewDialog(url, altText).open());
        return thumb;
    }
}
