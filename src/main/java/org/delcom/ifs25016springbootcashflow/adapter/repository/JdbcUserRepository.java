package org.delcom.ifs25016springbootcashflow.adapter.repository;

import org.delcom.ifs25016springbootcashflow.domain.entity.Role;
import org.delcom.ifs25016springbootcashflow.domain.entity.User;
import org.delcom.ifs25016springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Implementasi JDBC (PostgreSQL) untuk IUserRepository. */
public class JdbcUserRepository implements IUserRepository {
    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";
    private final DataSource ds;

    public JdbcUserRepository(DataSource ds) {
        this.ds = ds;
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"),
                rs.getString("full_name"), Role.valueOf(rs.getString("role")), rs.getString("photo_path"));
    }

    @Override
    public List<User> findAll() {
        return JdbcSupport.query(ds, "SELECT " + COLUMNS + " FROM users ORDER BY id", JdbcUserRepository::map);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return JdbcSupport.query(ds, "SELECT " + COLUMNS + " FROM users WHERE id = ?",
                JdbcUserRepository::map, id).stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return JdbcSupport.query(ds, "SELECT " + COLUMNS + " FROM users WHERE username = ?",
                JdbcUserRepository::map, username).stream().findFirst();
    }

    @Override
    public User save(User u) {
        int id = JdbcSupport.insert(ds,
                "INSERT INTO users (username, password_hash, full_name, role, photo_path) VALUES (?, ?, ?, ?, ?)",
                u.getUsername(), u.getPasswordHash(), u.getFullName(), u.getRole().name(), u.getPhotoPath());
        return new User(id, u.getUsername(), u.getPasswordHash(), u.getFullName(), u.getRole(), u.getPhotoPath());
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(0, username, passwordHash, fullName, role, null));
    }

    @Override
    public boolean deleteById(Integer id) {
        return JdbcSupport.update(ds, "DELETE FROM users WHERE id = ?", id) > 0;
    }

    @Override
    public void update(User u) {
        JdbcSupport.update(ds, "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?",
                u.getPasswordHash(), u.getFullName(), u.getPhotoPath(), u.getId());
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }
}
