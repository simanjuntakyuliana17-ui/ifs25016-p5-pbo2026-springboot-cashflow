package org.delcom.ifs25016springbootcashflow.usecase;

import org.delcom.ifs25016springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25016springbootcashflow.domain.entity.Role;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;

/** Proses registrasi, login, dan penyiapan admin awal. */
public class AuthUseCase {
    private static final int MIN_PASSWORD = 6;
    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) {
        this.users = users;
    }

    public User register(String fullName, String username, String password, String confirmPassword)
            throws ValidationException {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (username == null || !username.trim().matches("[A-Za-z0-9_.-]{3,30}")) {
            throw new ValidationException("Username 3-30 karakter (huruf, angka, titik, garis bawah, atau strip)");
        }
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD + " karakter");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Konfirmasi password tidak sama");
        }
        String name = username.trim();
        if (users.findByUsername(name).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return users.save(name, PasswordHasher.hash(password), fullName.trim(), Role.USER);
    }

    public User login(String username, String password) throws ValidationException {
        User user = username == null ? null : users.findByUsername(username.trim()).orElse(null);
        if (user == null || !PasswordHasher.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    /** Membuat admin awal dari konfigurasi ROOT_* bila belum ada. */
    public void ensureRootAdmin(String username, String password) {
        if (users.findByUsername(username).isEmpty()) {
            users.save(username, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
        }
    }
}
