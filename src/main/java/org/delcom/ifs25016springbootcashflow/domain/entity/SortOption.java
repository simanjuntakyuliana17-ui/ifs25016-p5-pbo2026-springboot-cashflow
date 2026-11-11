package org.delcom.ifs25016springbootcashflow.domain.entity;

import java.util.Comparator;

/** Kriteria pengurutan transaksi; setiap opsi membawa label dan Comparator-nya sendiri. */
public enum SortOption {
    AMOUNT_ASC("Jumlah terkecil", Comparator.comparingLong(Transaction::getAmount)),
    AMOUNT_DESC("Jumlah terbesar", Comparator.comparingLong(Transaction::getAmount).reversed()),
    INCOME_FIRST("Pemasukan dulu", Comparator.comparing(Transaction::getType)),
    EXPENSE_FIRST("Pengeluaran dulu", Comparator.comparing(Transaction::getType).reversed());

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
