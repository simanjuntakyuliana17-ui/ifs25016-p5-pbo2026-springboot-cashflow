package org.delcom.ifs25016springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import org.delcom.ifs25016springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;

import java.util.Comparator;

/** Grid transaksi dengan kolom baku; dipakai FinanceView dan UserTransactionsView (read-only). */
final class TransactionGrid {
    private TransactionGrid() {
    }

    static Grid<Transaction> create(FinancePresenter presenter, String label) {
        Grid<Transaction> grid = new Grid<>();
        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparingInt(Transaction::getId)).setSortable(true);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparingLong(Transaction::getAmount));
        grid.addColumn(presenter::formatType).setHeader("Tipe").setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));
        grid.addComponentColumn(t -> ImagePreviewDialog.thumbnail(t.getProofPath(), "Bukti transaksi " + t.getId()))
                .setHeader("Bukti").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(t -> presenter.formatDate(t.getCreatedAt())).setHeader("Waktu").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getCreatedAt));
        GridStyles.apply(grid, label);
        return grid;
    }
}
