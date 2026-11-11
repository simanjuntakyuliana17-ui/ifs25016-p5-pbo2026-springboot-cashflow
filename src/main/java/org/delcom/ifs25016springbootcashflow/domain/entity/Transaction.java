package org.delcom.ifs25016springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/** Entity transaksi keuangan milik satu pengguna. Jumlah disimpan sebagai bilangan bulat (rupiah). */
public class Transaction {
    private final int id;
    private final int userId;
    private String description;
    private long amount;
    private final TransactionType type;
    private String proofPath;
    private final LocalDateTime createdAt;

    public Transaction(int id, int userId, String description, long amount, TransactionType type,
                       String proofPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean hasProof() {
        return proofPath != null && !proofPath.isBlank();
    }

    /** Dampak ke saldo: positif untuk pemasukan, negatif untuk pengeluaran. */
    public long signedAmount() {
        return isIncome() ? amount : -amount;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeAmount(long amount) {
        this.amount = amount;
    }

    public void changeProofPath(String proofPath) {
        this.proofPath = proofPath;
    }
}
