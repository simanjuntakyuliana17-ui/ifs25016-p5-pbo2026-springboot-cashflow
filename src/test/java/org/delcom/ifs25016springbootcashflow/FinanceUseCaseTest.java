package org.delcom.ifs25016springbootcashflow;

import org.delcom.ifs25016springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25016springbootcashflow.domain.entity.Role;
import org.delcom.ifs25016springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25016springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25016springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceUseCaseTest {

    private FinanceUseCase useCase() {
        return new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void saldoPemasukanMenambahPengeluaranMengurangi() throws Exception {
        FinanceUseCase uc = useCase();
        uc.addIncome(1, "Gaji", "1000000", null);
        uc.addExpense(1, "Makan", "25000", null);
        assertEquals(1000000L, uc.getTotalIncome(1));
        assertEquals(25000L, uc.getTotalExpense(1));
        assertEquals(975000L, uc.getBalance(1));
    }

    @Test
    void dataTerpisahPerPengguna() throws Exception {
        FinanceUseCase uc = useCase();
        uc.addIncome(1, "A", "500", null);
        uc.addIncome(2, "B", "700", null);
        assertEquals(1, uc.getTransactions(1).size());
        assertEquals(700L, uc.getBalance(2));
    }

    @Test
    void validasiInput() {
        FinanceUseCase uc = useCase();
        assertThrows(ValidationException.class, () -> uc.addIncome(1, "  ", "1000", null));
        assertThrows(ValidationException.class, () -> uc.addIncome(1, null, "1000", null));
        assertThrows(ValidationException.class, () -> uc.addIncome(1, "x", null, null));
        assertThrows(ValidationException.class, () -> uc.addIncome(1, "x", " ", null));
        assertThrows(ValidationException.class, () -> uc.addIncome(1, "x", "abc", null));
        assertThrows(ValidationException.class, () -> uc.addExpense(1, "x", "0", null));
        assertThrows(ValidationException.class, () -> uc.addExpense(1, "x", "-5", null));
    }

    @Test
    void cariTanpaMembedakanHurufBesarKecil() throws Exception {
        FinanceUseCase uc = useCase();
        uc.addIncome(1, "Uang Bursar", "100", null);
        uc.addExpense(1, "Beli buku", "50", null);
        assertEquals(1, uc.search(1, "BURSAR").size());
        assertEquals(2, uc.search(1, null).size());
        assertEquals(0, uc.search(1, "zzz").size());
        assertEquals(0, uc.search(2, "bursar").size());
    }

    @Test
    void urutkanSemuaOpsi() throws Exception {
        FinanceUseCase uc = useCase();
        uc.addIncome(1, "a", "300", null);
        uc.addExpense(1, "b", "100", null);
        uc.addIncome(1, "c", "200", null);
        assertEquals(100L, uc.sort(1, SortOption.AMOUNT_ASC).get(0).getAmount());
        assertEquals(300L, uc.sort(1, SortOption.AMOUNT_DESC).get(0).getAmount());
        assertTrue(uc.sort(1, SortOption.INCOME_FIRST).get(0).isIncome());
        assertFalse(uc.sort(1, SortOption.EXPENSE_FIRST).get(0).isIncome());
    }

    @Test
    void hapusDanKepemilikan() throws Exception {
        FinanceUseCase uc = useCase();
        Transaction t = uc.addIncome(1, "a", "300", "bukti.png");
        assertThrows(ValidationException.class, () -> uc.remove(2, t.getId()));
        assertThrows(ValidationException.class, () -> uc.remove(1, 0));
        assertThrows(EntityNotFoundException.class, () -> uc.remove(1, 99));
        assertEquals("bukti.png", uc.remove(1, t.getId()).getProofPath());
        assertEquals(0L, uc.getBalance(1));
    }

    @Test
    void ubahTransaksiDanBukti() throws Exception {
        FinanceUseCase uc = useCase();
        Transaction t = uc.addExpense(1, "a", "300", null);
        uc.update(1, t.getId(), " baru ", "450");
        uc.updateProof(1, t.getId(), "p.png");
        Transaction saved = uc.getTransactions(1).get(0);
        assertEquals("baru", saved.getDescription());
        assertEquals(450L, saved.getAmount());
        assertTrue(saved.hasProof());
        assertThrows(ValidationException.class, () -> uc.update(1, t.getId(), "", "450"));
        assertThrows(ValidationException.class, () -> uc.update(1, t.getId(), "x", "0"));
        assertThrows(ValidationException.class, () -> uc.updateProof(2, t.getId(), "q.png"));
    }

    @Test
    void adminMelihatTransaksiPenggunaLain() throws Exception {
        FinanceUseCase uc = useCase();
        uc.addIncome(5, "a", "10", null);
        User admin = new User(1, "admin", "h", "Admin", Role.ADMIN, null);
        User biasa = new User(2, "u", "h", "U", Role.USER, null);
        List<Transaction> list = uc.viewAsAdmin(admin, 5);
        assertEquals(1, list.size());
        assertThrows(ValidationException.class, () -> uc.viewAsAdmin(biasa, 5));
        assertThrows(ValidationException.class, () -> uc.viewAsAdmin(null, 5));
    }
}
