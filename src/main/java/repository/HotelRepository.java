package repository;

import model.Hotel;
import model.Location;
import org.example.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HotelRepository {
    private static final Logger logger = LoggerFactory.getLogger(HotelRepository.class);
    private final LocationRepository locationRepository;

    public HotelRepository() {
        this.locationRepository = new LocationRepository();
    }

    public List<Hotel> findAll() {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotel ORDER BY nume";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Hotel hotel = mapResultSetToHotel(rs);

                // Load location data
                if (hotel.getLocationId() != null) {
                    Optional<Location> location = locationRepository.findById(hotel.getLocationId());
                    location.ifPresent(hotel::setLocation);
                }

                hotels.add(hotel);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all hotels", e);
        }

        return hotels;
    }

    public List<Hotel> findByChainId(Long chainId) {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotel WHERE id_lant = ? ORDER BY nume";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, chainId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Hotel hotel = mapResultSetToHotel(rs);

                    // Load location data
                    if (hotel.getLocationId() != null) {
                        Optional<Location> location = locationRepository.findById(hotel.getLocationId());
                        location.ifPresent(hotel::setLocation);
                    }

                    hotels.add(hotel);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching hotels by chain id: " + chainId, e);
        }

        return hotels;
    }

    public Optional<Hotel> findById(Long id) {
        String sql = "SELECT * FROM hotel WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Hotel hotel = mapResultSetToHotel(rs);

                    // Load location data
                    if (hotel.getLocationId() != null) {
                        Optional<Location> location = locationRepository.findById(hotel.getLocationId());
                        location.ifPresent(hotel::setLocation);
                    }

                    return Optional.of(hotel);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching hotel with id: " + id, e);
        }

        return Optional.empty();
    }

    public Long save(Hotel hotel) {
        String sql = "INSERT INTO hotel (nume, id_locatie, telefon, email, facilitati, id_lant) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hotel.getName());

            if (hotel.getLocationId() != null) {
                stmt.setLong(2, hotel.getLocationId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            stmt.setString(3, hotel.getPhone());
            stmt.setString(4, hotel.getEmail());
            stmt.setString(5, hotel.getAmenities());

            if (hotel.getChainId() != null) {
                stmt.setLong(6, hotel.getChainId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving hotel: " + hotel.getName(), e);
        }

        return null;
    }

    public boolean update(Hotel hotel) {
        String sql = "UPDATE hotel SET nume = ?, id_locatie = ?, telefon = ?, " +
                "email = ?, facilitati = ?, id_lant = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hotel.getName());

            if (hotel.getLocationId() != null) {
                stmt.setLong(2, hotel.getLocationId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            stmt.setString(3, hotel.getPhone());
            stmt.setString(4, hotel.getEmail());
            stmt.setString(5, hotel.getAmenities());

            if (hotel.getChainId() != null) {
                stmt.setLong(6, hotel.getChainId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }

            stmt.setLong(7, hotel.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating hotel with id: " + hotel.getId(), e);
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM hotel WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting hotel with id: " + id, e);
            return false;
        }
    }

    private Hotel mapResultSetToHotel(ResultSet rs) throws SQLException {
        Hotel hotel = new Hotel();
        hotel.setId(rs.getLong("id"));
        hotel.setName(rs.getString("nume"));

        Long locationId = rs.getLong("id_locatie");
        if (!rs.wasNull()) {
            hotel.setLocationId(locationId);
        }

        hotel.setPhone(rs.getString("telefon"));
        hotel.setEmail(rs.getString("email"));
        hotel.setAmenities(rs.getString("facilitati"));

        Long chainId = rs.getLong("id_lant");
        if (!rs.wasNull()) {
            hotel.setChainId(chainId);
        }

        return hotel;
    }
}