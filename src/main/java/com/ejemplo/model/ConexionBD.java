package com.ejemplo.model;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            String mysqlUrl = obtenerPrimeraNoVacia("MYSQL_URL");

            if (mysqlUrl != null) {
                return conectarConMysqlUrl(mysqlUrl);
            }

            String host = obtenerPrimeraNoVacia("DB_HOST", "MYSQLHOST");
            String port = obtenerPrimeraNoVacia("DB_PORT", "MYSQLPORT");
            String database = obtenerPrimeraNoVacia("DB_NAME", "MYSQLDATABASE");
            String user = obtenerPrimeraNoVacia("DB_USER", "MYSQLUSER");
            String password = obtenerPrimeraNoVacia("DB_PASSWORD", "MYSQLPASSWORD");

            if (host == null || port == null || database == null || user == null || password == null) {
                throw new SQLException(
                        "Faltan variables de base de datos. En Railway configura MYSQL_URL o bien MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER y MYSQLPASSWORD en el servicio de la app.");
            }

            String jdbcUrl = construirJdbcUrl(host, port, database);
            return DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException e) {
            throw new SQLException("Error de conexión: " + e.getMessage(), e);
        }
    }

    private static Connection conectarConMysqlUrl(String mysqlUrl) throws SQLException {
        try {
            URI uri = URI.create(mysqlUrl);
            String userInfo = uri.getUserInfo();

            if (userInfo == null || !userInfo.contains(":")) {
                throw new SQLException("MYSQL_URL no contiene credenciales válidas");
            }

            String[] partes = userInfo.split(":", 2);
            String user = partes[0];
            String password = partes[1];
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String database = uri.getPath() != null ? uri.getPath().replaceFirst("^/", "") : "";

            if (host == null || database.isBlank()) {
                throw new SQLException("MYSQL_URL no contiene host o base de datos válidos");
            }

            String jdbcUrl = construirJdbcUrl(host, String.valueOf(port), database);
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            return DriverManager.getConnection(jdbcUrl, props);
        } catch (IllegalArgumentException e) {
            throw new SQLException("MYSQL_URL no tiene un formato válido", e);
        }
    }

    private static String construirJdbcUrl(String host, String port, String database) {
        return "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static String obtenerPrimeraNoVacia(String... claves) {
        for (String clave : claves) {
            String valor = System.getenv(clave);
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }
        return null;
    }
}
