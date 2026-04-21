package com.ejemplo.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "mysql");
    private static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "bd1");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "root");

    private static final String URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            String sugerencia = "mysql".equals(DB_HOST)
                    ? " Configura DB_HOST, DB_PORT, DB_NAME, DB_USER y DB_PASSWORD en Render para una base de datos MySQL accesible."
                    : "";

            throw new SQLException(
                    "No se pudo conectar a MySQL en " + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "." + sugerencia
                            + " Motivo original: " + e.getMessage(),
                    e);
        }
    }
}
