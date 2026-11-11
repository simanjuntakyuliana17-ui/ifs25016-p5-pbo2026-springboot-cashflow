package org.delcom.ifs25016springbootcashflow.usecase;

import org.delcom.ifs25016springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25016springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25016springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

/** Proses pengelolaan pengguna dan profil. */
public class UserUseCase {
    private final IUserRepository users;

    public UserUseCase(IUserRepository users) {
        this.users = users;
    }

    /** Hanya ADMIN yang boleh melihat daftar seluruh pengguna. */
    public List<User> listUsers(User actor) throws ValidationException {
        requireAdmin(actor);
        return users.findAll();
    }

    public User getUser(int id) throws EntityNotFoundException {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /** Ubah nama dan (opsional) password. newPassword kosong = password tidak berubah. */
    public User updateProfile(int userId, String fullName, String newPassword) throws DomainException {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        User user = getUser(userId);
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                throw new ValidationException("Password minimal 6 karakter");
            }
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        user.changeFullName(fullName.trim());
        users.update(user);
        return user;
    }

    public User updatePhoto(int userId, String photoPath) throws EntityNotFoundException {
        User user = getUser(userId);
        user.changePhotoPath(photoPath);
        users.update(user);
        return user;
    }

    static void requireAdmin(User actor) throws ValidationException {
        if (actor == null || !actor.isAdmin()) {
            throw new ValidationException("Hanya admin yang boleh mengakses data ini");
        }
    }
}
