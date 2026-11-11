package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid yang konsisten di seluruh halaman. */
public final class GridStyles {
    private GridStyles() {
    }

    public static void apply(Grid<?> grid, String accessibleLabel) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.getElement().setAttribute("aria-label", accessibleLabel);
        grid.setHeight("26rem");
    }
}
