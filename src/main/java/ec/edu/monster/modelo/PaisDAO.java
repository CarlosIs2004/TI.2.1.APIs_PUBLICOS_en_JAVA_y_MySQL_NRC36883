package ec.edu.monster.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaisDAO {
    
    public boolean guardar(Pais pais){
        String sql = "INSERT INTO pais(nombre, capital, region, poblacion, moneda) VALUES(?,?,?,?,?)";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, pais.getNombre());
            ps.setString(2, pais.getCapital());
            ps.setString(3, pais.getRegion());
            ps.setLong(4, pais.getPoblacion());
            ps.setString(5, pais.getMoneda());
            
            ps.executeUpdate();
            return true;
        } catch(Exception e){
            System.out.println("Error en guardar DAO: " + e.getMessage());
        }
        return false;
    }

    /** Convierte la fila actual del ResultSet en un objeto Pais. */
    private Pais mapear(ResultSet rs) throws SQLException {
        Pais p = new Pais();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setCapital(rs.getString("capital"));
        p.setRegion(rs.getString("region"));
        p.setPoblacion(rs.getLong("poblacion"));
        p.setMoneda(rs.getString("moneda"));
        return p;
    }

    public List<Pais> listar() {
        List<Pais> lista = new ArrayList<>();
        String sql = "SELECT * FROM pais";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (Exception e) {
            System.out.println("Error al listar DAO: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizar(Pais pais) {
        String sql = "UPDATE pais SET nombre=?, capital=?, region=?, poblacion=?, moneda=? WHERE id=?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, pais.getNombre());
            ps.setString(2, pais.getCapital());
            ps.setString(3, pais.getRegion());
            ps.setLong(4, pais.getPoblacion());
            ps.setString(5, pais.getMoneda());
            ps.setInt(6, pais.getId());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar DAO: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM pais WHERE id=?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar DAO: " + e.getMessage());
            return false;
        }
    }

    public List<Pais> buscarPorRegion(String region) {
        List<Pais> lista = new ArrayList<>();
        String sql = "SELECT * FROM pais WHERE region = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("Error al filtrar por región: " + e.getMessage());
        }
        return lista;
    }

    /** Busca países cuyo nombre contenga el texto indicado (sin distinguir mayúsculas). */
    public List<Pais> buscarPorNombre(String nombre) {
        List<Pais> lista = new ArrayList<>();
        String sql = "SELECT * FROM pais WHERE nombre LIKE ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("Error al buscar por nombre: " + e.getMessage());
        }
        return lista;
    }

    /** Indica si ya existe un país con exactamente ese nombre (evita duplicados). */
    public boolean existePorNombre(String nombre) {
        String sql = "SELECT 1 FROM pais WHERE nombre = ? LIMIT 1";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.out.println("Error al verificar existencia: " + e.getMessage());
        }
        return false;
    }

    /** Elimina todos los registros de la tabla y reinicia el contador de IDs. */
    public boolean vaciar() {
        String sql = "TRUNCATE TABLE pais";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error al vaciar la tabla: " + e.getMessage());
            return false;
        }
    }

    public String obtenerMasPoblado() {
        String sql = "SELECT nombre, poblacion FROM pais ORDER BY poblacion DESC LIMIT 1";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("nombre") + " (" + rs.getLong("poblacion") + " hab.)";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Sin datos";
    }

    /** Cantidad de países por región, de mayor a menor. */
    public Map<String, Integer> contarPaisesPorRegion() {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        String sql = "SELECT region, COUNT(*) AS total FROM pais GROUP BY region ORDER BY total DESC, region";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteo.put(rs.getString("region"), rs.getInt("total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conteo;
    }
}