-- Database is created by MYSQL_DATABASE; this creates the table from the workshop.
USE api_paises;

CREATE TABLE IF NOT EXISTS pais (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    capital VARCHAR(100),
    region VARCHAR(100),
    poblacion BIGINT,
    moneda VARCHAR(100)
);
