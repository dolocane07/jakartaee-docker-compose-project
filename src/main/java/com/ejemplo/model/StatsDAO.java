package com.ejemplo.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ejemplo.util.ErrorUtil;

public class StatsDAO {

    public List<TagCount> topRelationships(int userId, int limit) {
        String sql = """
                SELECT r.name, COUNT(*) AS total
                FROM fanfic_relationship fr
                JOIN relationships r ON r.id = fr.relationship_id
                JOIN fanfics f ON f.id = fr.fanfic_id
                WHERE f.user_id = ?
                GROUP BY r.id, r.name
                ORDER BY total DESC, r.name ASC
                LIMIT ?
                """;
        return consultarTop(sql, userId, limit);
    }

    public List<TagCount> topFandoms(int userId, int limit) {
        String sql = """
                SELECT fdm.name, COUNT(*) AS total
                FROM fanfic_fandom ff
                JOIN fandoms fdm ON fdm.id = ff.fandom_id
                JOIN fanfics f ON f.id = ff.fanfic_id
                WHERE f.user_id = ?
                GROUP BY fdm.id, fdm.name
                ORDER BY total DESC, fdm.name ASC
                LIMIT ?
                """;
        return consultarTop(sql, userId, limit);
    }

    public List<TagCount> topWarnings(int userId, int limit) {
        String sql = """
                SELECT w.name, COUNT(*) AS total
                FROM fanfic_warning fw
                JOIN warnings w ON w.id = fw.warning_id
                JOIN fanfics f ON f.id = fw.fanfic_id
                WHERE f.user_id = ?
                GROUP BY w.id, w.name
                ORDER BY total DESC, w.name ASC
                LIMIT ?
                """;
        return consultarTop(sql, userId, limit);
    }

    public Map<String, Integer> ratingAo3Breakdown(int userId) {
        String sql = """
                SELECT ao3_rating, COUNT(*) AS total
                FROM fanfics
                WHERE user_id = ?
                GROUP BY ao3_rating
                ORDER BY total DESC, ao3_rating ASC
                """;
        return consultarMapaTexto(sql, userId, "ao3_rating");
    }

    public Map<String, Integer> estrellasBreakdown(int userId) {
        String sql = """
                SELECT CAST(user_stars AS CHAR) AS clave, COUNT(*) AS total
                FROM fanfics
                WHERE user_id = ?
                GROUP BY user_stars
                ORDER BY user_stars DESC
                """;
        return consultarMapaTexto(sql, userId, "clave");
    }

    public Map<String, Integer> categoriesBreakdown(int userId) {
        String sql = """
                SELECT c.name, COUNT(*) AS total
                FROM fanfic_category fc
                JOIN categories c ON c.id = fc.category_id
                JOIN fanfics f ON f.id = fc.fanfic_id
                WHERE f.user_id = ?
                GROUP BY c.id, c.name
                ORDER BY total DESC, c.name ASC
                """;
        return consultarMapaTexto(sql, userId, "name");
    }

    public int mediaPalabras(int userId) {
        String sql = "SELECT COALESCE(ROUND(AVG(word_count)), 0) AS media FROM fanfics WHERE user_id = ?";

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("media");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular la media de palabras: " + ErrorUtil.getRootCauseMessage(e), e);
        }
    }

    private List<TagCount> consultarTop(String sql, int userId, int limit) {
        List<TagCount> lista = new ArrayList<>();

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new TagCount(rs.getString("name"), rs.getInt("total")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar estadisticas: " + ErrorUtil.getRootCauseMessage(e), e);
        }

        return lista;
    }

    private Map<String, Integer> consultarMapaTexto(String sql, int userId, String columnaClave) {
        Map<String, Integer> mapa = new LinkedHashMap<>();

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(rs.getString(columnaClave), rs.getInt("total"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar estadisticas: " + ErrorUtil.getRootCauseMessage(e), e);
        }

        return mapa;
    }
}
