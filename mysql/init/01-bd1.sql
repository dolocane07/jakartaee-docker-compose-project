CREATE DATABASE IF NOT EXISTS bd1;
USE bd1;

CREATE TABLE IF NOT EXISTS contactos (
    ide_con INT NOT NULL AUTO_INCREMENT,
    nom_con VARCHAR(120) NOT NULL,
    tlf_con INT NOT NULL,
    PRIMARY KEY (ide_con)
);

INSERT INTO contactos (nom_con, tlf_con) VALUES
('Ana López', 600111222),
('Borja Martín', 611222333),
('Carlos Pérez', 622333444),
('Diana Ruiz', 633444555),
('Elena Gómez', 644555666),
('Francisco Torres', 655666777),
('Gabriela Navarro', 666777888);
