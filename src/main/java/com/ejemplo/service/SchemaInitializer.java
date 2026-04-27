package com.ejemplo.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ejemplo.model.ConexionBD;

public class SchemaInitializer {

    private static volatile boolean initialized = false;

    public void ensureSchema() {
        if (initialized) {
            return;
        }

        synchronized (SchemaInitializer.class) {
            if (initialized) {
                return;
            }

            try (Connection conexion = ConexionBD.getConnection()) {
                crearTablaUsers(conexion);
                crearTablaFanfics(conexion);
                crearTablasCatalogo(conexion);
                crearTablasRelacion(conexion);
                initialized = true;
            } catch (Exception e) {
                throw new RuntimeException("No se pudo inicializar el esquema de la base de datos", e);
            }
        }
    }

    private void crearTablaUsers(Connection conexion) throws Exception {
        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS users (
                    id INT NOT NULL AUTO_INCREMENT,
                    username VARCHAR(60) NOT NULL,
                    email VARCHAR(180) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id)
                )
                """);

        if (!indiceExiste(conexion, "users", "uk_users_username")) {
            ejecutar(conexion, "CREATE UNIQUE INDEX uk_users_username ON users (username)");
        }

        if (!indiceExiste(conexion, "users", "uk_users_email")) {
            ejecutar(conexion, "CREATE UNIQUE INDEX uk_users_email ON users (email)");
        }
    }

    private void crearTablaFanfics(Connection conexion) throws Exception {
        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS fanfics (
                    id INT NOT NULL AUTO_INCREMENT,
                    user_id INT NULL,
                    ao3_url VARCHAR(500) NOT NULL,
                    ao3_work_id VARCHAR(50) NULL,
                    titulo VARCHAR(500) NOT NULL,
                    autor VARCHAR(500) NOT NULL,
                    ao3_rating VARCHAR(120) NOT NULL,
                    word_count INT NOT NULL DEFAULT 0,
                    finished_date DATE NOT NULL,
                    user_stars TINYINT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id)
                )
                """);

        if (!columnaExiste(conexion, "fanfics", "user_id")) {
            ejecutar(conexion, "ALTER TABLE fanfics ADD COLUMN user_id INT NULL AFTER id");
        }

        ejecutar(conexion, "ALTER TABLE fanfics MODIFY ao3_url VARCHAR(500) NOT NULL");
        ejecutar(conexion, "ALTER TABLE fanfics MODIFY titulo VARCHAR(500) NOT NULL");
        ejecutar(conexion, "ALTER TABLE fanfics MODIFY autor VARCHAR(500) NOT NULL");

        eliminarIndiceSiExiste(conexion, "fanfics", "uk_fanfics_ao3_url");
        eliminarIndiceSiExiste(conexion, "fanfics", "uk_fanfics_ao3_work_id");

        if (!indiceExiste(conexion, "fanfics", "idx_fanfics_user_id")) {
            ejecutar(conexion, "CREATE INDEX idx_fanfics_user_id ON fanfics (user_id)");
        }

        if (!indiceExiste(conexion, "fanfics", "uk_fanfics_user_ao3_url")) {
            ejecutar(conexion, "CREATE UNIQUE INDEX uk_fanfics_user_ao3_url ON fanfics (user_id, ao3_url)");
        }

        if (!indiceExiste(conexion, "fanfics", "uk_fanfics_user_ao3_work_id")) {
            ejecutar(conexion, "CREATE UNIQUE INDEX uk_fanfics_user_ao3_work_id ON fanfics (user_id, ao3_work_id)");
        }
    }

    private void crearTablasCatalogo(Connection conexion) throws Exception {
        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS fandoms (
                    id INT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(500) NOT NULL,
                    PRIMARY KEY (id)
                )
                """);

        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS relationships (
                    id INT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(500) NOT NULL,
                    PRIMARY KEY (id)
                )
                """);

        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS warnings (
                    id INT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(500) NOT NULL,
                    PRIMARY KEY (id)
                )
                """);

        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS categories (
                    id INT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(150) NOT NULL,
                    PRIMARY KEY (id)
                )
                """);

        ejecutar(conexion, "ALTER TABLE fandoms MODIFY name VARCHAR(500) NOT NULL");
        ejecutar(conexion, "ALTER TABLE relationships MODIFY name VARCHAR(500) NOT NULL");
        ejecutar(conexion, "ALTER TABLE warnings MODIFY name VARCHAR(500) NOT NULL");
        ejecutar(conexion, "ALTER TABLE categories MODIFY name VARCHAR(150) NOT NULL");

        if (!indiceExiste(conexion, "fandoms", "uk_fandoms_name")) {
            ejecutar(conexion, "CREATE UNIQUE INDEX uk_fandoms_name ON fandoms (name)");
        }

        if (!indiceExiste(conexion, "relationships", "uk_relationships_name")) {
            ejecutar(conexion, "CREATE UNIQUE INDEX uk_relationships_name ON relationships (name)");
        }

        if (!indiceExiste(conexion, "warnings", "uk_warnings_name")) {
            ejecutar(conexion, "CREATE UNIQUE INDEX uk_warnings_name ON warnings (name)");
        }

        if (!indiceExiste(conexion, "categories", "uk_categories_name")) {
            ejecutar(conexion, "CREATE UNIQUE INDEX uk_categories_name ON categories (name)");
        }
    }

    private void crearTablasRelacion(Connection conexion) throws Exception {
        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS fanfic_fandom (
                    fanfic_id INT NOT NULL,
                    fandom_id INT NOT NULL,
                    PRIMARY KEY (fanfic_id, fandom_id),
                    CONSTRAINT fk_fanfic_fandom_fanfic FOREIGN KEY (fanfic_id) REFERENCES fanfics (id) ON DELETE CASCADE,
                    CONSTRAINT fk_fanfic_fandom_fandom FOREIGN KEY (fandom_id) REFERENCES fandoms (id) ON DELETE CASCADE
                )
                """);

        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS fanfic_relationship (
                    fanfic_id INT NOT NULL,
                    relationship_id INT NOT NULL,
                    PRIMARY KEY (fanfic_id, relationship_id),
                    CONSTRAINT fk_fanfic_relationship_fanfic FOREIGN KEY (fanfic_id) REFERENCES fanfics (id) ON DELETE CASCADE,
                    CONSTRAINT fk_fanfic_relationship_relationship FOREIGN KEY (relationship_id) REFERENCES relationships (id) ON DELETE CASCADE
                )
                """);

        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS fanfic_warning (
                    fanfic_id INT NOT NULL,
                    warning_id INT NOT NULL,
                    PRIMARY KEY (fanfic_id, warning_id),
                    CONSTRAINT fk_fanfic_warning_fanfic FOREIGN KEY (fanfic_id) REFERENCES fanfics (id) ON DELETE CASCADE,
                    CONSTRAINT fk_fanfic_warning_warning FOREIGN KEY (warning_id) REFERENCES warnings (id) ON DELETE CASCADE
                )
                """);

        ejecutar(conexion, """
                CREATE TABLE IF NOT EXISTS fanfic_category (
                    fanfic_id INT NOT NULL,
                    category_id INT NOT NULL,
                    PRIMARY KEY (fanfic_id, category_id),
                    CONSTRAINT fk_fanfic_category_fanfic FOREIGN KEY (fanfic_id) REFERENCES fanfics (id) ON DELETE CASCADE,
                    CONSTRAINT fk_fanfic_category_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
                )
                """);
    }

    private void eliminarIndiceSiExiste(Connection conexion, String tabla, String indice) throws Exception {
        if (indiceExiste(conexion, tabla, indice)) {
            ejecutar(conexion, "ALTER TABLE " + tabla + " DROP INDEX " + indice);
        }
    }

    private boolean columnaExiste(Connection conexion, String tabla, String columna) throws Exception {
        DatabaseMetaData metadata = conexion.getMetaData();
        try (ResultSet rs = metadata.getColumns(conexion.getCatalog(), null, tabla, columna)) {
            return rs.next();
        }
    }

    private boolean indiceExiste(Connection conexion, String tabla, String indice) throws Exception {
        DatabaseMetaData metadata = conexion.getMetaData();
        try (ResultSet rs = metadata.getIndexInfo(conexion.getCatalog(), null, tabla, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName != null && indexName.equalsIgnoreCase(indice)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ejecutar(Connection conexion, String sql) throws Exception {
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.execute();
        }
    }
}
