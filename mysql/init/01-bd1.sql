CREATE DATABASE IF NOT EXISTS bd1;
USE bd1;

DROP TABLE IF EXISTS fanfic_category;
DROP TABLE IF EXISTS fanfic_warning;
DROP TABLE IF EXISTS fanfic_relationship;
DROP TABLE IF EXISTS fanfic_fandom;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS warnings;
DROP TABLE IF EXISTS relationships;
DROP TABLE IF EXISTS fandoms;
DROP TABLE IF EXISTS fanfics;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(60) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE fanfics (
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
    PRIMARY KEY (id),
    KEY idx_fanfics_user_id (user_id),
    UNIQUE KEY uk_fanfics_user_ao3_url (user_id, ao3_url),
    UNIQUE KEY uk_fanfics_user_ao3_work_id (user_id, ao3_work_id)
);

CREATE TABLE fandoms (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fandoms_name (name)
);

CREATE TABLE relationships (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_relationships_name (name)
);

CREATE TABLE warnings (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_warnings_name (name)
);

CREATE TABLE categories (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name)
);

CREATE TABLE fanfic_fandom (
    fanfic_id INT NOT NULL,
    fandom_id INT NOT NULL,
    PRIMARY KEY (fanfic_id, fandom_id),
    CONSTRAINT fk_fanfic_fandom_fanfic FOREIGN KEY (fanfic_id) REFERENCES fanfics (id) ON DELETE CASCADE,
    CONSTRAINT fk_fanfic_fandom_fandom FOREIGN KEY (fandom_id) REFERENCES fandoms (id) ON DELETE CASCADE
);

CREATE TABLE fanfic_relationship (
    fanfic_id INT NOT NULL,
    relationship_id INT NOT NULL,
    PRIMARY KEY (fanfic_id, relationship_id),
    CONSTRAINT fk_fanfic_relationship_fanfic FOREIGN KEY (fanfic_id) REFERENCES fanfics (id) ON DELETE CASCADE,
    CONSTRAINT fk_fanfic_relationship_relationship FOREIGN KEY (relationship_id) REFERENCES relationships (id) ON DELETE CASCADE
);

CREATE TABLE fanfic_warning (
    fanfic_id INT NOT NULL,
    warning_id INT NOT NULL,
    PRIMARY KEY (fanfic_id, warning_id),
    CONSTRAINT fk_fanfic_warning_fanfic FOREIGN KEY (fanfic_id) REFERENCES fanfics (id) ON DELETE CASCADE,
    CONSTRAINT fk_fanfic_warning_warning FOREIGN KEY (warning_id) REFERENCES warnings (id) ON DELETE CASCADE
);

CREATE TABLE fanfic_category (
    fanfic_id INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (fanfic_id, category_id),
    CONSTRAINT fk_fanfic_category_fanfic FOREIGN KEY (fanfic_id) REFERENCES fanfics (id) ON DELETE CASCADE,
    CONSTRAINT fk_fanfic_category_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);
