package org.delcom.ifs25016springbootcashflow.adapter.repository;

import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25016springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25016springbootcashflow.domain.repository.ITransactionRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Implementasi JDBC (PostgreSQL) untuk ITransactionRepository. */
public class JdbcTransactionRepository implements ITransactionRepository {
    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";
    private final DataSource ds;

    public JdbcTransactionRepository(DataSource ds) {
        this.ds = ds;
    }

    private static Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                rs.getLong("amount"), TransactionType.valueOf(rs.getString("type")), rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public List<Transaction> findAll() {
        return JdbcSupport.query(ds, "SELECT " + COLUMNS + " FROM transactions ORDER BY id",
                JdbcTransactionRepository::map);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return JdbcSupport.query(ds, "SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY id",
                JdbcTransactionRepository::map, userId);
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return JdbcSupport.query(ds, "SELECT " + COLUMNS + " FROM transactions WHERE id = ?",
                JdbcTransactionRepository::map, id).stream().findFirst();
    }

    @Override
    public Transaction save(Transaction t) {
        int id = JdbcSupport.insert(ds,
                "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                t.getUserId(), t.getDescription(), t.getAmount(), t.getType().name(), t.getProofPath(),
                Timestamp.valueOf(t.getCreatedAt()));
        return new Transaction(id, t.getUserId(), t.getDescription(), t.getAmount(), t.getType(),
                t.getProofPath(), t.getCreatedAt());
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(0, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public boolean deleteById(Integer id) {
        return JdbcSupport.update(ds, "DELETE FROM transactions WHERE id = ?", id) > 0;
    }

    @Override
    public void update(Transaction t) {
        JdbcSupport.update(ds, "UPDATE transactions SET description = ?, amount = ?, proof_path = ? WHERE id = ?",
                t.getDescription(), t.getAmount(), t.getProofPath(), t.getId());
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }
}
