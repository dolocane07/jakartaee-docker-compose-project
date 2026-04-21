package com.ejemplo.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ejemplo.util.ErrorUtil;

public class FanficDAO {

    public boolean existePorUrl(String ao3Url) {
        String sql = "SELECT 1 FROM fanfics WHERE ao3_url = ?";

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, ao3Url);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al comprobar si el fanfic ya existe: " + ErrorUtil.getRootCauseMessage(e), e);
        }
    }

    public int guardar(Fanfic fanfic) {
        String sql = """
                INSERT INTO fanfics (
                    ao3_url, ao3_work_id, titulo, autor, ao3_rating, word_count, finished_date, user_stars
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.getConnection()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, fanfic.getAo3Url());
                ps.setString(2, fanfic.getAo3WorkId());
                ps.setString(3, fanfic.getTitulo());
                ps.setString(4, fanfic.getAutor());
                ps.setString(5, fanfic.getAo3Rating());
                ps.setInt(6, fanfic.getWordCount());
                ps.setDate(7, Date.valueOf(fanfic.getFinishedDate()));
                ps.setInt(8, fanfic.getUserStars());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        fanfic.setId(rs.getInt(1));
                    }
                }
            }

            guardarTags(conexion, fanfic.getId(), fanfic.getFandoms(), "fandoms", "fandom_id", "fanfic_fandom");
            guardarTags(conexion, fanfic.getId(), fanfic.getRelationships(), "relationships", "relationship_id", "fanfic_relationship");
            guardarTags(conexion, fanfic.getId(), fanfic.getWarnings(), "warnings", "warning_id", "fanfic_warning");
            guardarTags(conexion, fanfic.getId(), fanfic.getCategories(), "categories", "category_id", "fanfic_category");

            conexion.commit();
            return fanfic.getId();
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el fanfic: " + ErrorUtil.getRootCauseMessage(e), e);
        }
    }

    public List<Fanfic> listarTodos() {
        List<Fanfic> fanfics = new ArrayList<>();

        String sql = """
                SELECT id, ao3_url, ao3_work_id, titulo, autor, ao3_rating, word_count, finished_date, user_stars
                FROM fanfics
                ORDER BY finished_date DESC, created_at DESC
                """;

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Fanfic fanfic = new Fanfic();
                fanfic.setId(rs.getInt("id"));
                fanfic.setAo3Url(rs.getString("ao3_url"));
                fanfic.setAo3WorkId(rs.getString("ao3_work_id"));
                fanfic.setTitulo(rs.getString("titulo"));
                fanfic.setAutor(rs.getString("autor"));
                fanfic.setAo3Rating(rs.getString("ao3_rating"));
                fanfic.setWordCount(rs.getInt("word_count"));
                fanfic.setFinishedDate(rs.getDate("finished_date").toString());
                fanfic.setUserStars(rs.getInt("user_stars"));
                fanfic.setFandoms(cargarTags(conexion, fanfic.getId(), "fandoms", "fandom_id", "fanfic_fandom"));
                fanfic.setRelationships(cargarTags(conexion, fanfic.getId(), "relationships", "relationship_id", "fanfic_relationship"));
                fanfic.setWarnings(cargarTags(conexion, fanfic.getId(), "warnings", "warning_id", "fanfic_warning"));
                fanfic.setCategories(cargarTags(conexion, fanfic.getId(), "categories", "category_id", "fanfic_category"));
                fanfics.add(fanfic);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al listar fanfics: " + ErrorUtil.getRootCauseMessage(e), e);
        }

        return fanfics;
    }

    public int contarFanfics() {
        String sql = "SELECT COUNT(*) FROM fanfics";

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException("Error al contar fanfics: " + ErrorUtil.getRootCauseMessage(e), e);
        }
    }

    private void guardarTags(Connection conexion, int fanficId, List<String> tags, String tablaCatalogo,
                             String columnaCatalogoId, String tablaRelacion) throws Exception {
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }

            int catalogoId = obtenerOCrearCatalogo(conexion, tablaCatalogo, tag.trim());
            insertarRelacion(conexion, fanficId, catalogoId, columnaCatalogoId, tablaRelacion);
        }
    }

    private int obtenerOCrearCatalogo(Connection conexion, String tablaCatalogo, String nombre) throws Exception {
        String insertSql = "INSERT INTO " + tablaCatalogo + " (name) VALUES (?) ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)";

        try (PreparedStatement ps = conexion.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new IllegalStateException("No se pudo obtener el id del catalogo " + tablaCatalogo);
    }

    private void insertarRelacion(Connection conexion, int fanficId, int catalogoId, String columnaCatalogoId,
                                  String tablaRelacion) throws Exception {
        String sql = "INSERT INTO " + tablaRelacion + " (fanfic_id, " + columnaCatalogoId + ") VALUES (?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, fanficId);
            ps.setInt(2, catalogoId);
            ps.executeUpdate();
        }
    }

    private List<String> cargarTags(Connection conexion, int fanficId, String tablaCatalogo, String columnaCatalogoId,
                                    String tablaRelacion) throws Exception {
        List<String> tags = new ArrayList<>();

        String sql = """
                SELECT c.name
                FROM %s r
                JOIN %s c ON c.id = r.%s
                WHERE r.fanfic_id = ?
                ORDER BY c.name ASC
                """.formatted(tablaRelacion, tablaCatalogo, columnaCatalogoId);

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, fanficId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("name"));
                }
            }
        }

        return tags;
    }
}
