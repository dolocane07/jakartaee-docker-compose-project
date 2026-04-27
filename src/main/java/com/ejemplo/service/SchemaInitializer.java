package com.ejemplo.service;

import java.sql.Connection;
import java.sql.PreparedStatement;

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
                ejecutar(conexion, """
                        CREATE TABLE IF NOT EXISTS fanfics (
                            id INT NOT NULL AUTO_INCREMENT,
                            ao3_url VARCHAR(500) NOT NULL,
                            ao3_work_id VARCHAR(50) NULL,
                            titulo VARCHAR(500) NOT NULL,
                            autor VARCHAR(500) NOT NULL,
                            ao3_rating VARCHAR(120) NOT NULL,
                            word_count INT NOT NULL DEFAULT 0,
                            finished_date DATE NOT NULL,
                            user_stars TINYINT NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_fanfics_ao3_url (ao3_url),
                            UNIQUE KEY uk_fanfics_ao3_work_id (ao3_work_id)
                        )
                        """);

                ejecutar(conexion, """
                        CREATE TABLE IF NOT EXISTS fandoms (
                            id INT NOT NULL AUTO_INCREMENT,
                            name VARCHAR(500) NOT NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_fandoms_name (name)
                        )
                        """);

                ejecutar(conexion, """
                        CREATE TABLE IF NOT EXISTS relationships (
                            id INT NOT NULL AUTO_INCREMENT,
                            name VARCHAR(500) NOT NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_relationships_name (name)
                        )
                        """);

                ejecutar(conexion, """
                        CREATE TABLE IF NOT EXISTS warnings (
                            id INT NOT NULL AUTO_INCREMENT,
                            name VARCHAR(500) NOT NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_warnings_name (name)
                        )
                        """);

                ejecutar(conexion, """
                        CREATE TABLE IF NOT EXISTS categories (
                            id INT NOT NULL AUTO_INCREMENT,
                            name VARCHAR(150) NOT NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_categories_name (name)
                        )
                        """);

                ejecutar(conexion, "ALTER TABLE fanfics MODIFY titulo VARCHAR(500) NOT NULL");
                ejecutar(conexion, "ALTER TABLE fanfics MODIFY autor VARCHAR(500) NOT NULL");
                ejecutar(conexion, "ALTER TABLE fandoms MODIFY name VARCHAR(500) NOT NULL");
                ejecutar(conexion, "ALTER TABLE relationships MODIFY name VARCHAR(500) NOT NULL");
                ejecutar(conexion, "ALTER TABLE warnings MODIFY name VARCHAR(500) NOT NULL");
                ejecutar(conexion, "ALTER TABLE categories MODIFY name VARCHAR(150) NOT NULL");

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

                initialized = true;
            } catch (Exception e) {
                throw new RuntimeException("No se pudo inicializar el esquema de la base de datos", e);
            }
        }
    }

    private void ejecutar(Connection conexion, String sql) throws Exception {
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.execute();
        }
    }
}
