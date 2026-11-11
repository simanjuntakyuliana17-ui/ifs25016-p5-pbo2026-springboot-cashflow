package org.delcom.ifs25016springbootcashflow.adapter.presenter;

import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Memformat data transaksi menjadi teks siap tampil. Entity tidak mengurus format. */
public class FinancePresenter {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** 1000000 -> "Rp 1,000,000.00". */
    public String formatMoney(long amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "Rp " + df.format(amount);
    }

    public String formatType(Transaction t) {
        return t.getType().label();
    }

    public String formatDate(LocalDateTime time) {
        return time.format(DATE_TIME);
    }

    /** [id] keterangan | Rp 1,000,000.00 | [Pemasukan] */
    public String format(Transaction t) {
        return String.format("[%d] %s | %s | [%s]", t.getId(), t.getDescription(),
                formatMoney(t.getAmount()), formatType(t));
    }

    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String showAddSuccess(Transaction t) {
        return "Berhasil menambah transaksi: " + format(t);
    }

    public String showUpdateSuccess(Transaction t) {
        return "Berhasil mengubah transaksi: " + format(t);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showEmpty() {
        return "Data transaksi belum tersedia.";
    }

    public String showNotFound() {
        return "Transaksi tidak ditemukan.";
    }

    public String showError(String message) {
        return "[!] " + message;
    }
}
