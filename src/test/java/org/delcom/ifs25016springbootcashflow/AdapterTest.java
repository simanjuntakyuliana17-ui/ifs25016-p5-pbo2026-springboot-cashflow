package org.delcom.ifs25016springbootcashflow;

import org.delcom.ifs25016springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25016springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25016springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25016springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25016springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25016springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdapterTest {

    @Test
    void presenterFormatUangDanBaris() {
        FinancePresenter p = new FinancePresenter();
        Transaction t = new Transaction(3, 1, "Gaji", 1000000, TransactionType.INCOME, null,
                LocalDateTime.of(2026, 11, 5, 11, 34));
        assertEquals("Rp 1,000,000.00", p.formatMoney(1000000));
        assertEquals("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]", p.format(t));
        assertEquals("Saldo: Rp 4,975,000.00", p.formatBalance(4975000));
        assertEquals("05/11/2026 11:34", p.formatDate(t.getCreatedAt()));
        assertEquals("Pemasukan", p.formatType(t));
        assertTrue(p.showAddSuccess(t).contains("Gaji"));
        assertTrue(p.showUpdateSuccess(t).contains("Gaji"));
        assertEquals("[!] gagal", p.showError("gagal"));
        assertFalse(p.showRemoveSuccess().isBlank());
        assertFalse(p.showEmpty().isBlank());
        assertFalse(p.showNotFound().isBlank());
    }

    @Test
    void entityTransaksi() {
        Transaction in = new Transaction(1, 1, "a", 10, TransactionType.INCOME, "x.png", LocalDateTime.now());
        Transaction out = new Transaction(2, 1, "b", 10, TransactionType.EXPENSE, " ", LocalDateTime.now());
        assertEquals(10L, in.signedAmount());
        assertEquals(-10L, out.signedAmount());
        assertTrue(in.hasProof());
        assertFalse(out.hasProof());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.label());
    }

    @Test
    void repositoryInMemoryCrudDanSalinanDefensif() {
        TransactionRepository repo = new TransactionRepository();
        Transaction a = repo.save(1, "a", 10, TransactionType.INCOME, null);
        repo.save(2, "b", 20, TransactionType.EXPENSE, null);
        List<Transaction> copy = repo.findAll();
        copy.clear();
        assertEquals(2, repo.findAll().size());
        assertEquals(1, repo.findByUserId(1).size());
        a.changeAmount(99);
        repo.update(a);
        assertEquals(99L, repo.findById(a.getId()).orElseThrow().getAmount());
        repo.update(new Transaction(500, 1, "x", 1, TransactionType.INCOME, null, LocalDateTime.now()));
        assertEquals(2, repo.findAll().size());
        assertTrue(repo.deleteById(a.getId()));
        assertFalse(repo.deleteById(a.getId()));
        assertTrue(repo.findById(a.getId()).isEmpty());
    }

    @Test
    void passwordHasher() {
        String h = PasswordHasher.hash("abc");
        assertEquals(64, h.length());
        assertTrue(PasswordHasher.matches("abc", h));
        assertFalse(PasswordHasher.matches("abd", h));
        assertFalse(PasswordHasher.matches(null, h));
        assertFalse(PasswordHasher.matches("abc", null));
    }

    @Test
    void exceptionMembawaPesanDanId() {
        EntityNotFoundException ex = new EntityNotFoundException("Transaksi", 7);
        assertEquals(7, ex.getId());
        assertTrue(ex.getMessage().contains("7"));
        assertEquals("x", new ValidationException("x").getMessage());
    }

    @Test
    void fileStorageSimpanHapusDanValidasi() throws Exception {
        Path dir = Files.createTempDirectory("cashflow-upload");
        FileStorage storage = new FileStorage(dir.toString());
        String name = storage.save("Bukti.PNG", new ByteArrayInputStream(new byte[]{1, 2, 3}));
        assertTrue(name.endsWith(".png"));
        assertTrue(Files.exists(dir.resolve(name)));
        assertTrue(storage.resourceLocation().endsWith("/"));
        assertEquals(dir.toAbsolutePath().normalize(), storage.root());
        assertThrows(ValidationException.class, () -> storage.save("virus.exe", new ByteArrayInputStream(new byte[]{1})));
        assertThrows(ValidationException.class, () -> storage.save("tanpaekstensi", new ByteArrayInputStream(new byte[]{1})));
        assertThrows(ValidationException.class, () -> storage.save(null, new ByteArrayInputStream(new byte[]{1})));
        assertThrows(ValidationException.class,
                () -> storage.save("besar.jpg", new ByteArrayInputStream(new byte[FileStorage.MAX_BYTES + 1])));
        storage.delete("../" + name);
        assertFalse(Files.exists(dir.resolve(name)));
        storage.delete(null);
        storage.delete(" ");
        storage.delete("tidak-ada.png");
    }
}
