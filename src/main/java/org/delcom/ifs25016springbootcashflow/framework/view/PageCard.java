package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

/** Kartu konten halaman (latar putih, sudut membulat). */
public class PageCard extends Div {
    public PageCard(Component... components) {
        addClassName("page-card");
        add(components);
    }
}
