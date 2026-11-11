package org.delcom.ifs25016springbootcashflow;

import org.delcom.ifs25016springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25016springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25016springbootcashflow.adapter.repository.RepositoryException;
import org.delcom.ifs25016springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25016springbootcashflow.domain.entity.Role;
import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25016springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Tes repository JDBC memakai H2 (mode PostgreSQL), tanpa PostgreSQL sungguhan. */
class JdbcRepositoryTest {

    private DataSource newDb() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        SchemaInitializer.initialize(ds);
        SchemaInitializer.initialize(ds); // idempotent
        return ds;
    }

    @Test
    void userCrud() {
        JdbcUserRepository repo = new JdbcUserRepository(newDb());
        User saved = repo.save("ifs25016", "hash", "Yuliana", Role.USER);
        assertTrue(saved.getId() > 0);
        assertEquals("Yuliana", repo.findByUsername("ifs25016").orElseThrow().getFullName());
        assertTrue(repo.findByUsername("tidakada").isEmpty());
        saved.changeFullName("Baru");
        saved.changePhotoPath("f.png");
        repo.update(saved);
        User loaded = repo.findById(saved.getId()).orElseThrow();
        assertEquals("Baru", loaded.getFullName());
        assertEquals("f.png", loaded.getPhotoPath());
        assertEquals(Role.USER, loaded.getRole());
        assertEquals(1, repo.findAll().size());
        assertEquals(1, repo.findBy(u -> u.getUsername().startsWith("ifs")).size());
        assertTrue(repo.deleteById(saved.getId()));
        assertFalse(repo.deleteById(saved.getId()));
    }

    @Test
    void transactionCrud() {
        DataSource ds = newDb();
        User u = new JdbcUserRepository(ds).save("a", "h", "A", Role.ADMIN);
        JdbcTransactionRepository repo = new JdbcTransactionRepository(ds);
        Transaction in = repo.save(u.getId(), "Gaji", 5000000, TransactionType.INCOME, null);
        Transaction out = repo.save(u.getId(), "Makan", 25000, TransactionType.EXPENSE, "b.png");
        assertEquals(2, repo.findByUserId(u.getId()).size());
        assertEquals(0, repo.findByUserId(999).size());
        Transaction loaded = repo.findById(out.getId()).orElseThrow();
        assertEquals("b.png", loaded.getProofPath());
        assertEquals(TransactionType.EXPENSE, loaded.getType());
        assertEquals(25000L, loaded.getAmount());
        assertNotNull(loaded.getCreatedAt());
        in.changeAmount(6000000);
        in.changeDescription("Gaji baru");
        in.changeProofPath("g.png");
        repo.update(in);
        assertEquals(6000000L, repo.findById(in.getId()).orElseThrow().getAmount());
        assertEquals(2, repo.findAll().size());
        assertEquals(1, repo.findBy(Transaction::isIncome).size());
        assertTrue(repo.deleteById(in.getId()));
        assertFalse(repo.deleteById(in.getId()));
        assertTrue(repo.findById(in.getId()).isEmpty());
    }

    @Test
    void kegagalanSqlDibungkusRepositoryException() {
        DataSource ds = newDb();
        JdbcUserRepository repo = new JdbcUserRepository(ds);
        repo.save("dup", "h", "D", Role.USER);
        assertThrows(RepositoryException.class, () -> repo.save("dup", "h", "D2", Role.USER));
        JdbcTransactionRepository tx = new JdbcTransactionRepository(ds);
        assertThrows(RepositoryException.class, () -> tx.save(12345, "x", 1, TransactionType.INCOME, null)); // FK
    }
}
