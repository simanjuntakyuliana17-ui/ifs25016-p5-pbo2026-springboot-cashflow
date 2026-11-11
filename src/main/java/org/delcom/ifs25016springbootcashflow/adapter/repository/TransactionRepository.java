package org.delcom.ifs25016springbootcashflow.adapter.repository;

import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25016springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25016springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Implementasi in-memory ITransactionRepository dengan ID otomatis (untuk unit test). */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(++idCounter, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.getUserId() == userId);
    }
}
