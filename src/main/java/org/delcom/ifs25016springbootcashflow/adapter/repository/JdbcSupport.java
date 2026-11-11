package org.delcom.ifs25016springbootcashflow.adapter.repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Helper JDBC kecil: query, update, dan insert dengan parameter terikat (anti SQL injection). */
final class JdbcSupport {

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private JdbcSupport() {
    }

    static <T> List<T> query(DataSource ds, String sql, RowMapper<T> mapper, Object... params) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Query gagal: " + sql, ex);
        }
    }

    static int update(DataSource ds, String sql, Object... params) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Update gagal: " + sql, ex);
        }
    }

    /** Menjalankan INSERT dan mengembalikan ID yang dihasilkan database (kolom pertama = id). */
    static int insert(DataSource ds, String sql, Object... params) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Insert gagal: " + sql, ex);
        }
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                ps.setNull(i + 1, Types.VARCHAR);
            } else {
                ps.setObject(i + 1, params[i]);
            }
        }
    }
}
