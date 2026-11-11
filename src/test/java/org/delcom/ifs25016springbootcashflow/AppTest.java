package org.delcom.ifs25016springbootcashflow;

import org.delcom.ifs25016springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25016springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25016springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25016springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25016springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/** Smoke test: seluruh bean terhubung memakai profil test (H2), lalu satu alur end-to-end lewat JDBC. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private ITransactionRepository transactionRepository;
    @Autowired
    private AuthUseCase authUseCase;
    @Autowired
    private UserUseCase userUseCase;
    @Autowired
    private FinanceUseCase financeUseCase;
    @Autowired
    private FinancePresenter presenter;
    @Autowired
    private FileStorage fileStorage;

    @Test
    void semuaBeanTerhubung() {
        assertNotNull(userRepository);
        assertNotNull(transactionRepository);
        assertNotNull(userUseCase);
        assertNotNull(presenter);
        assertNotNull(fileStorage);
    }

    @Test
    void adminRootTerseedDariKonfigurasi() throws Exception {
        assertTrue(authUseCase.login("admin", "admin").isAdmin());
    }

    @Test
    void alurRegisterTransaksiSaldoLewatJdbc() throws Exception {
        User u = authUseCase.register("Penguji", "penguji01", "rahasia1", "rahasia1");
        financeUseCase.addIncome(u.getId(), "Gaji", "1000000", null);
        financeUseCase.addExpense(u.getId(), "Makan", "25000", null);
        assertEquals(975000L, financeUseCase.getBalance(u.getId()));
        assertEquals("Saldo: Rp 975,000.00", presenter.formatBalance(financeUseCase.getBalance(u.getId())));
    }
}
