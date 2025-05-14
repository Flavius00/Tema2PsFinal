package repository;

import model.Chain;
import model.Hotel;
import model.Location;
import org.example.tema2ps.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HotelRepository {
    private static final Logger logger = LoggerFactory.getLogger(HotelRepository.class);
    private final LocationRepository locationRepository;
    private final ChainRepository chainRepository;

    public HotelRepository() {
        this.locationRepository = new LocationRepository();
        this.chainRepository = new ChainRepository();
    }

    public List<Hotel> findAll() {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT h.*, l.id as loc_id, l.tara, l.oras, l.strada, l.numar, " +
                "c.id as chain_id, c.nume as chain_name " +
                "FROM hotel h " +
                "LEFT JOIN locatie l ON h.id_locatie = l.id " +
                "LEFT JOIN lant c ON h.id_lant = c.id " +
                "ORDER BY h.nume";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Hotel hotel = mapResultSetToHotel(rs);

                // Set Location directly from join results
                if (hotel.getLocationId() != null) {
                    Location location = new Location();
                    location.setId(rs.getLong("loc_id"));
                    location.setCountry(rs.getString("tara"));
                    location.setCity(rs.getString("oras"));
                    location.setStreet(rs.getString("strada"));
                    location.setNumber(rs.getString("numar"));
                    hotel.setLocation(location);
                }

                // Set Chain directly from join results
                if (hotel.getChainId() != null) {
                    Chain chain = new Chain();
                    chain.setId(rs.getLong("chain_id"));
                    chain.setName(rs.getString("chain_name"));
                    hotel.setChain(chain);
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
        String sql = "SELECT h.*, l.id as loc_id, l.tara, l.oras, l.strada, l.numar, " +
                "c.id as chain_id, c.nume as chain_name " +
                "FROM hotel h " +
                "LEFT JOIN locatie l ON h.id_locatie = l.id " +
                "LEFT JOIN lant c ON h.id_lant = c.id " +
                "WHERE h.id_lant = ? " +
                "ORDER BY h.nume";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, chainId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Hotel hotel = mapResultSetToHotel(rs);

                    // Set Location directly from join results
                    if (hotel.getLocationId() != null) {
                        Location location = new Location();
                        location.setId(rs.getLong("loc_id"));
                        location.setCountry(rs.getString("tara"));
                        location.setCity(rs.getString("oras"));
                        location.setStreet(rs.getString("strada"));
                        location.setNumber(rs.getString("numar"));
                        hotel.setLocation(location);
                    }

                    // Set Chain directly from join results
                    if (hotel.getChainId() != null) {
                        Chain chain = new Chain();
                        chain.setId(rs.getLong("chain_id"));
                        chain.setName(rs.getString("chain_name"));
                        hotel.setChain(chain);
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
        String sql = "SELECT h.*, l.id as loc_id, l.tara, l.oras, l.strada, l.numar, " +
                "c.id as chain_id, c.nume as chain_name " +
                "FROM hotel h " +
                "LEFT JOIN locatie l ON h.id_locatie = l.id " +
                "LEFT JOIN lant c ON h.id_lant = c.id " +
                "WHERE h.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Hotel hotel = mapResultSetToHotel(rs);

                    // Set Location directly from join results
                    if (hotel.getLocationId() != null) {
                        Location location = new Location();
                        location.setId(rs.getLong("loc_id"));
                        location.setCountry(rs.getString("tara"));
                        location.setCity(rs.getString("oras"));
                        location.setStreet(rs.getString("strada"));
                        location.setNumber(rs.getString("numar"));
                        hotel.setLocation(location);
                    }

                    // Set Chain directly from join results
                    if (hotel.getChainId() != null) {
                        Chain chain = new Chain();
                        chain.setId(rs.getLong("chain_id"));
                        chain.setName(rs.getString("chain_name"));
                        hotel.setChain(chain);
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
                "VALUES (?, ?, ?, ?, ?, ?)";
        String[] generatedColumns = {"id"};

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return null;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
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