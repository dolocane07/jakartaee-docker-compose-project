package com.ejemplo.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String MYSQL_URL = System.getenv("MYSQL_URL");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            if (MYSQL_URL == null) {
                throw new SQLException("MYSQL_URL no está configurada");
            }

            String url = MYSQL_URL.replace("mysql://", "jdbc:mysql://");

            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new SQLException("Error de conexión: " + e.getMessage(), e);
        }
    }
}