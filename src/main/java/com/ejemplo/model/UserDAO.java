package com.ejemplo.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ejemplo.util.ErrorUtil;
import com.ejemplo.util.PasswordUtil;

public class UserDAO {

    public User registrar(String username, String email, String password) {
        String sql = """
                INSERT INTO users (username, email, password_hash, is_admin)
                VALUES (?, ?, ?, 0)
                """;

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hashPassword(password));
            ps.executeUpdate();

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }

            return user;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo registrar la cuenta: " + ErrorUtil.getRootCauseMessage(e), e);
        }
    }

    public User buscarPorIdentifier(String identifier) {
        String sql = """
                SELECT id, username, email, password_hash
                       , is_admin
                FROM users
                WHERE username = ? OR email = ?
                LIMIT 1
                """;

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, identifier);
            ps.setString(2, identifier);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo buscar la cuenta: " + ErrorUtil.getRootCauseMessage(e), e);
        }

        return null;
    }

    public User buscarPorId(int id) {
        String sql = """
                SELECT id, username, email, password_hash
                       , is_admin
                FROM users
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar la cuenta: " + ErrorUtil.getRootCauseMessage(e), e);
        }

        return null;
    }

    public boolean existeUsername(String username) {
        return existePorCampo("username", username);
    }

    public boolean existeEmail(String email) {
        return existePorCampo("email", email);
    }

    public List<Map<String, Object>> listarUsuariosConTotales() {
        String sql = """
                SELECT u.id,
                       u.username,
                       u.email,
                       u.is_admin,
                       u.created_at,
                       COUNT(f.id) AS fanfic_count
                FROM users u
                LEFT JOIN fanfics f ON f.user_id = u.id
                GROUP BY u.id, u.username, u.email, u.is_admin, u.created_at
                ORDER BY u.created_at DESC, u.username ASC
                """;

        List<Map<String, Object>> usuarios = new ArrayList<>();

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("id", rs.getInt("id"));
                usuario.put("username", rs.getString("username"));
                usuario.put("email", rs.getString("email"));
                usuario.put("isAdmin", rs.getBoolean("is_admin"));
                usuario.put("createdAt", rs.getTimestamp("created_at").toString());
                usuario.put("fanficCount", rs.getInt("fanfic_count"));
                usuarios.add(usuario);
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo listar los usuarios: " + ErrorUtil.getRootCauseMessage(e), e);
        }

        return usuarios;
    }

    public void eliminarCuentaComoAdmin(int userId, int adminUserId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Debes seleccionar una cuenta valida");
        }

        if (userId == adminUserId) {
            throw new IllegalArgumentException("El admin no puede borrar su propia cuenta");
        }

        String validarSql = "SELECT is_admin FROM users WHERE id = ? LIMIT 1";
        String deleteFanficsSql = "DELETE FROM fanfics WHERE user_id = ?";
        String deleteUserSql = "DELETE FROM users WHERE id = ?";

        Connection conexion = null;

        try {
            conexion = ConexionBD.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement validar = conexion.prepareStatement(validarSql)) {
                validar.setInt(1, userId);

                try (ResultSet rs = validar.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("No se encontro la cuenta seleccionada");
                    }

                    if (rs.getBoolean("is_admin")) {
                        throw new IllegalArgumentException("No se pueden borrar cuentas admin");
                    }
                }
            }

            try (PreparedStatement deleteFanfics = conexion.prepareStatement(deleteFanficsSql)) {
                deleteFanfics.setInt(1, userId);
                deleteFanfics.executeUpdate();
            }

            try (PreparedStatement deleteUser = conexion.prepareStatement(deleteUserSql)) {
                deleteUser.setInt(1, userId);

                if (deleteUser.executeUpdate() == 0) {
                    throw new IllegalArgumentException("No se encontro la cuenta seleccionada");
                }
            }

            conexion.commit();
        } catch (IllegalArgumentException e) {
            rollbackSilencioso(conexion);
            throw e;
        } catch (Exception e) {
            rollbackSilencioso(conexion);
            throw new RuntimeException("No se pudo borrar la cuenta: " + ErrorUtil.getRootCauseMessage(e), e);
        } finally {
            cerrarSilencioso(conexion);
        }
    }

    private boolean existePorCampo(String campo, String valor) {
        String sql = "SELECT 1 FROM users WHERE " + campo + " = ? LIMIT 1";

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, valor);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo validar la cuenta: " + ErrorUtil.getRootCauseMessage(e), e);
        }
    }

    private User mapear(ResultSet rs) throws Exception {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setAdmin(rs.getBoolean("is_admin"));
        return user;
    }

    private void rollbackSilencioso(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.rollback();
            } catch (Exception ignored) {
            }
        }
    }

    private void cerrarSilencioso(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (Exception ignored) {
            }
        }
    }
}
