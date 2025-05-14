package repository;

import model.Location;
import org.example.tema2ps.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationRepository {
    private static final Logger logger = LoggerFactory.getLogger(LocationRepository.class);

    public List<Location> findAll() {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT * FROM locatie ORDER BY tara, oras";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Location location = mapResultSetToLocation(rs);
                locations.add(location);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all locations", e);
        }

        return locations;
    }

    public Optional<Location> findById(Long id) {
        String sql = "SELECT * FROM locatie WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLocation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching location with id: " + id, e);
        }

        return Optional.empty();
    }

    public Long save(Location location) {
        String sql = "INSERT INTO locatie (tara, oras, strada, numar) VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location.getCountry());
            stmt.setString(2, location.getCity());
            stmt.setString(3, location.getStreet());
            stmt.setString(4, location.getNumber());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving location: " + location.getCity() + ", " + location.getCountry(), e);
        }

        return null;
    }

    public boolean update(Location location) {
        String sql = "UPDATE locatie SET tara = ?, oras = ?, strada = ?, numar = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location.getCountry());
            stmt.setString(2, location.getCity());
            stmt.setString(3, location.getStreet());
            stmt.setString(4, location.getNumber());
            stmt.setLong(5, location.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating location with id: " + location.getId(), e);
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM locatie WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting location with id: " + id, e);
            return false;
        }
    }

    private Location mapResultSetToLocation(ResultSet rs) throws SQLException {
        Location location = new Location();
        location.setId(rs.getLong("id"));
        location.setCountry(rs.getString("tara"));
        location.setCity(rs.getString("oras"));
        location.setStreet(rs.getString("strada"));
        location.setNumber(rs.getString("numar"));
        return location;
    }
}