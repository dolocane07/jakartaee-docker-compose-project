package com.ejemplo.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ContactoDAO {

    public List<Contacto> buscarPorNombre(String textoBusqueda) {
        List<Contacto> lista = new ArrayList<>();

        String sql = "SELECT ide_con, nom_con, tlf_con FROM contactos WHERE nom_con LIKE ? ORDER BY nom_con ASC";

        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, "%" + textoBusqueda + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Contacto contacto = new Contacto();
                    contacto.setIdeCon(rs.getInt("ide_con"));
                    contacto.setNomCon(rs.getString("nom_con"));
                    contacto.setTlfCon(rs.getInt("tlf_con"));
                    lista.add(contacto);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al buscar contactos", e);
        }

        return lista;
    }
}
