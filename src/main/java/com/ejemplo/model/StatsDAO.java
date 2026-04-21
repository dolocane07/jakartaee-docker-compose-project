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

    public List<TagCount> topRelationships(int limit) {
        String sql = """
                SELECT r.name, COUNT(*) AS total
                FROM fanfic_relationship fr
                JOIN relationships r ON r.id = fr.relationship_id
                GROUP BY r.id, r.name
                ORDER BY total DESC, r.name ASC
                LIMIT ?
                """;
        return consultarTop(sql, limit);
    }

    public List<TagCount> topFandoms(int limit) {
        String sql = """
                SELECT f.name, COUNT(*) AS total
                FROM fanfic_fandom ff
                JOIN fandoms f ON f.id = ff.fandom_id
                GROUP BY f.id, f.name
                ORDER BY total DESC, f.name ASC
                LIMIT ?
                """;
        return consultarTop(sql, limit);
    }

    public List<TagCount> topWarnings(int limit) {
        String sql = """
                SELECT w.name, COUNT(*) AS total
                FROM fanfic_warning fw
                JOIN warnings w ON w.id = fw.warning_id
                GROUP BY w.id, w.name
                ORDER BY total DESC, w.name ASC
                LIMIT ?
                """;
        return consultarTop(sql, limit);
    }

    public Map<String, Integer> ratingAo3Breakdown() {
        String sql = """
                SELECT ao3_rating, COUNT(*) AS total
                FROM fanfics
                GROUP BY ao3_rating
                ORDER BY total DESC, ao3_rating ASC
                """;
        return consultarMapaTexto(sql, "ao3_rating");
    }

    public Map<String, Integer> estrellasBreakdown() {
        String sql = """
                SELECT CAST(user_stars AS CHAR) AS clave, COUNT(*) AS total
                FROM fanfics
                GROUP BY user_stars
                ORDER BY user_stars DESC
                """;
        return consultarMapaTexto(sql, "clave");
    }

    public Map<String, Integer> categoriesBreakdown() {
        String sql = """
                SELECT c.name, COUNT(*) AS total
                FROM fanfic_category fc
                JOIN categories c ON c.id = fc.category_id
                GROUP BY c.id, c.name
                ORDER BY total DESC, c.name ASC
                """;
        return consultarMapaTexto(sql, "name");
    }

    public int mediaPalabras() {
        String sql = "SELECT COALESCE(ROUND(AVG(word_count)), 0) AS media FROM fanfics";

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt("media");
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular la media de palabras: " + ErrorUtil.getRootCauseMessage(e), e);
        }
    }

    private List<TagCount> consultarTop(String sql, int limit) {
        List<TagCount> lista = new ArrayList<>();

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, limit);

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

    private Map<String, Integer> consultarMapaTexto(String sql, String columnaClave) {
        Map<String, Integer> mapa = new LinkedHashMap<>();

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapa.put(rs.getString(columnaClave), rs.getInt("total"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar estadisticas: " + ErrorUtil.getRootCauseMessage(e), e);
        }

        return mapa;
    }
}
