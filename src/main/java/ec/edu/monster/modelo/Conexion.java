package ec.edu.monster.modelo;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    // Values come from environment variables or the local .env file (see env.example).
    private static final String URL = "jdbc:mysql://localhost:"
            + Config.get("MYSQL_PORT", "3306") + "/"
            + Config.get("MYSQL_DATABASE", "api_paises");
    private static final String USER = "root";
    private static final String PASS = Config.get("MYSQL_ROOT_PASSWORD", "");

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch(Exception e) {
            System.out.println("Error de conexión: " + e.getMessage());
            return null;
        }
    }
}