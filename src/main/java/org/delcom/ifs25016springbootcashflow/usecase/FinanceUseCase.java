package org.delcom.ifs25016springbootcashflow.usecase;

import org.delcom.ifs25016springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25016springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25016springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25016springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Proses bisnis pencatatan keuangan. Semua operasi dibatasi pada pengguna tertentu (userId).
 * Tidak ada I/O UI; kegagalan dilaporkan lewat checked domain exception.
 */
public class FinanceUseCase {
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> getTransactions(int userId) {
        return repository.findByUserId(userId);
    }

    public Transaction addIncome(int userId, String description, String amountText, String proofPath)
            throws ValidationException {
        return add(userId, description, amountText, TransactionType.INCOME, proofPath);
    }

    public Transaction addExpense(int userId, String description, String amountText, String proofPath)
            throws ValidationException {
        return add(userId, description, amountText, TransactionType.EXPENSE, proofPath);
    }

    public List<Transaction> search(int userId, String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findByUserId(userId).stream()
                .filter(t -> t.getDescription().toLowerCase().contains(key)).toList();
    }

    public List<Transaction> sort(int userId, SortOption option) {
        return repository.findByUserId(userId).stream().sorted(option.comparator()).toList();
    }

    public Transaction update(int userId, int id, String description, String amountText) throws DomainException {
        Transaction t = findOwned(userId, id);
        String desc = validateDescription(description);
        long amount = parseAmount(amountText);
        t.changeDescription(desc);
        t.changeAmount(amount);
        repository.update(t);
        return t;
    }

    public void updateProof(int userId, int id, String proofPath) throws DomainException {
        Transaction t = findOwned(userId, id);
        t.changeProofPath(proofPath);
        repository.update(t);
    }

    /** Menghapus transaksi milik sendiri; mengembalikan transaksi terhapus (untuk membersihkan file bukti). */
    public Transaction remove(int userId, int id) throws DomainException {
        Transaction t = findOwned(userId, id);
        repository.deleteById(id);
        return t;
    }

    public long getTotalIncome(int userId) {
        return total(userId, true);
    }

    public long getTotalExpense(int userId) {
        return total(userId, false);
    }

    /** Saldo = total pemasukan - total pengeluaran. */
    public long getBalance(int userId) {
        return repository.findByUserId(userId).stream().mapToLong(Transaction::signedAmount).sum();
    }

    /** Ringkasan total dalam satu kali baca data (menggantikan 4 query terpisah di dashboard). */
    public record Summary(int count, long income, long expense, long balance) {
    }

    public Summary summary(int userId) {
        List<Transaction> all = repository.findByUserId(userId);
        long income = all.stream().filter(Transaction::isIncome).mapToLong(Transaction::getAmount).sum();
        long expense = all.stream().filter(t -> !t.isIncome()).mapToLong(Transaction::getAmount).sum();
        return new Summary(all.size(), income, expense, income - expense);
    }

    /** ADMIN meninjau transaksi pengguna lain (read-only). */
    public List<Transaction> viewAsAdmin(User actor, int targetUserId) throws ValidationException {
        UserUseCase.requireAdmin(actor);
        return repository.findByUserId(targetUserId);
    }

    private long total(int userId, boolean income) {
        return repository.findByUserId(userId).stream()
                .filter(t -> t.isIncome() == income).mapToLong(Transaction::getAmount).sum();
    }

    private Transaction add(int userId, String description, String amountText, TransactionType type,
                            String proofPath) throws ValidationException {
        String desc = validateDescription(description);
        long amount = parseAmount(amountText);
        return repository.save(userId, desc, amount, type, proofPath);
    }

    private Transaction findOwned(int userId, int id) throws DomainException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        Transaction t = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (t.getUserId() != userId) {
            throw new ValidationException("Transaksi ini bukan milik Anda");
        }
        return t;
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private long parseAmount(String text) throws ValidationException {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        long amount;
        try {
            amount = Long.parseLong(text.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka bulat");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        return amount;
    }
}
