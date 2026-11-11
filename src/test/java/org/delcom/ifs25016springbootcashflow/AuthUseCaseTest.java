package org.delcom.ifs25016springbootcashflow;

import org.delcom.ifs25016springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25016springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25016springbootcashflow.domain.entity.Role;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25016springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25016springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthUseCaseTest {

    @Test
    void registerDanLogin() throws Exception {
        UserRepository repo = new UserRepository();
        AuthUseCase auth = new AuthUseCase(repo);
        User u = auth.register(" Yuliana ", "ifs25016", "rahasia1", "rahasia1");
        assertEquals(Role.USER, u.getRole());
        assertEquals("Yuliana", u.getFullName());
        assertNotEquals("rahasia1", u.getPasswordHash());
        assertEquals(u.getId(), auth.login("ifs25016", "rahasia1").getId());
    }

    @Test
    void registerValidasi() throws Exception {
        AuthUseCase auth = new AuthUseCase(new UserRepository());
        assertThrows(ValidationException.class, () -> auth.register("", "abc", "123456", "123456"));
        assertThrows(ValidationException.class, () -> auth.register(null, "abc", "123456", "123456"));
        assertThrows(ValidationException.class, () -> auth.register("N", "a b", "123456", "123456"));
        assertThrows(ValidationException.class, () -> auth.register("N", null, "123456", "123456"));
        assertThrows(ValidationException.class, () -> auth.register("N", "abc", "123", "123"));
        assertThrows(ValidationException.class, () -> auth.register("N", "abc", null, null));
        assertThrows(ValidationException.class, () -> auth.register("N", "abc", "123456", "654321"));
        auth.register("N", "abc", "123456", "123456");
        assertThrows(ValidationException.class, () -> auth.register("N", "abc", "123456", "123456"));
    }

    @Test
    void loginGagal() throws Exception {
        AuthUseCase auth = new AuthUseCase(new UserRepository());
        auth.register("N", "abc", "123456", "123456");
        assertThrows(ValidationException.class, () -> auth.login("abc", "salah"));
        assertThrows(ValidationException.class, () -> auth.login("tidakada", "123456"));
        assertThrows(ValidationException.class, () -> auth.login(null, "123456"));
    }

    @Test
    void rootAdminHanyaDibuatSekali() throws Exception {
        UserRepository repo = new UserRepository();
        AuthUseCase auth = new AuthUseCase(repo);
        auth.ensureRootAdmin("admin", "admin");
        auth.ensureRootAdmin("admin", "lain");
        assertEquals(1, repo.findAll().size());
        assertTrue(auth.login("admin", "admin").isAdmin());
    }

    @Test
    void kelolaProfil() throws Exception {
        UserRepository repo = new UserRepository();
        AuthUseCase auth = new AuthUseCase(repo);
        UserUseCase users = new UserUseCase(repo);
        User u = auth.register("N", "abc", "123456", "123456");
        users.updateProfile(u.getId(), "Nama Baru", "");
        assertEquals("Nama Baru", users.getUser(u.getId()).getFullName());
        assertTrue(PasswordHasher.matches("123456", users.getUser(u.getId()).getPasswordHash()));
        users.updateProfile(u.getId(), "Nama Baru", "passbaru1");
        assertTrue(PasswordHasher.matches("passbaru1", users.getUser(u.getId()).getPasswordHash()));
        users.updateProfile(u.getId(), "Nama Baru", null);
        assertThrows(ValidationException.class, () -> users.updateProfile(u.getId(), " ", null));
        assertThrows(ValidationException.class, () -> users.updateProfile(u.getId(), "X", "123"));
        assertThrows(EntityNotFoundException.class, () -> users.updateProfile(99, "X", null));
        assertTrue(users.updatePhoto(u.getId(), "f.png").hasPhoto());
        assertFalse(users.updatePhoto(u.getId(), null).hasPhoto());
        assertThrows(EntityNotFoundException.class, () -> users.getUser(99));
    }

    @Test
    void daftarPenggunaHanyaAdmin() throws Exception {
        UserRepository repo = new UserRepository();
        AuthUseCase auth = new AuthUseCase(repo);
        UserUseCase users = new UserUseCase(repo);
        auth.ensureRootAdmin("admin", "admin");
        User biasa = auth.register("N", "abc", "123456", "123456");
        assertEquals(2, users.listUsers(auth.login("admin", "admin")).size());
        assertThrows(ValidationException.class, () -> users.listUsers(biasa));
        assertThrows(ValidationException.class, () -> users.listUsers(null));
    }
}
