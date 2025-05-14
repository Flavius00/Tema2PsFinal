package repository;

import model.Hotel;
import model.Room;
import org.example.tema2ps.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RoomRepository {
    private static final Logger logger = LoggerFactory.getLogger(RoomRepository.class);
    private final HotelRepository hotelRepository;

    public RoomRepository() {
        this.hotelRepository = new HotelRepository();
    }

    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM camera ORDER BY id_hotel, nr_camera";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Room room = mapResultSetToRoom(rs);

                // Load hotel data
                if (room.getHotelId() != null) {
                    Optional<Hotel> hotel = hotelRepository.findById(room.getHotelId());
                    hotel.ifPresent(room::setHotel);
                }

                rooms.add(room);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all rooms", e);
        }

        return rooms;
    }

    public List<Room> findByHotelId(Long hotelId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM camera WHERE id_hotel = ? ORDER BY nr_camera";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, hotelId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Room room = mapResultSetToRoom(rs);

                    // Load hotel data
                    Optional<Hotel> hotel = hotelRepository.findById(hotelId);
                    hotel.ifPresent(room::setHotel);

                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            logger.error("Eroare la obținerea camerelor după id-ul hotelului: " + hotelId, e);
        }

        return rooms;
    }

    public List<Room> findByHotelIdAndPrice(Long hotelId, Double minPrice, Double maxPrice) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM camera WHERE id_hotel = ? AND pret_per_noapte BETWEEN ? AND ? ORDER BY pret_per_noapte";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, hotelId);
            stmt.setDouble(2, minPrice);
            stmt.setDouble(3, maxPrice);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Room room = mapResultSetToRoom(rs);

                    // Load hotel data
                    Optional<Hotel> hotel = hotelRepository.findById(hotelId);
                    hotel.ifPresent(room::setHotel);

                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            logger.error("Eroare la obținerea camerelor după id-ul hotelului și intervalul de preț: " + hotelId, e);
        }

        return rooms;
    }

    public List<Room> findAvailableRoomsByHotelIdAndDate(Long hotelId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT c.* FROM camera c WHERE c.id_hotel = ? " +
                "AND c.id NOT IN (SELECT r.id_camera FROM rezervari r WHERE " +
                "((r.start_date <= ? AND r.end_date >= ?) OR " +
                "(r.start_date <= ? AND r.end_date >= ?) OR " +
                "(r.start_date >= ? AND r.end_date <= ?)))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, hotelId);
            stmt.setTimestamp(2, Timestamp.valueOf(startDate));
            stmt.setTimestamp(3, Timestamp.valueOf(startDate));
            stmt.setTimestamp(4, Timestamp.valueOf(endDate));
            stmt.setTimestamp(5, Timestamp.valueOf(endDate));
            stmt.setTimestamp(6, Timestamp.valueOf(startDate));
            stmt.setTimestamp(7, Timestamp.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Room room = mapResultSetToRoom(rs);

                    // Load hotel data
                    Optional<Hotel> hotel = hotelRepository.findById(hotelId);
                    hotel.ifPresent(room::setHotel);

                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            logger.error("Eroare la obținerea camerelor disponibile după id-ul hotelului și dată: " + hotelId, e);
        }

        return rooms;
    }

    public Optional<Room> findById(Long id) {
        String sql = "SELECT * FROM camera WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Room room = mapResultSetToRoom(rs);

                    // Load hotel data
                    if (room.getHotelId() != null) {
                        Optional<Hotel> hotel = hotelRepository.findById(room.getHotelId());
                        hotel.ifPresent(room::setHotel);
                    }

                    return Optional.of(room);
                }
            }
        } catch (SQLException e) {
            logger.error("Eroare la obținerea camerei cu id-ul: " + id, e);
        }

        return Optional.empty();
    }

    public Long save(Room room) {
        String sql = "INSERT INTO camera (id_hotel, nr_camera, pret_per_noapte, id_poze, facilitati, tip_camera, capacitate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String[] generatedColumns = {"id"};

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (room.getHotelId() != null) {
                stmt.setLong(1, room.getHotelId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }

            stmt.setString(2, room.getRoomNumber());
            stmt.setDouble(3, room.getPricePerNight());

            if (room.getImageId() != null) {
                stmt.setLong(4, room.getImageId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }

            stmt.setString(5, room.getAmenities());
            stmt.setString(6, room.getRoomType());

            // Actualizat: Folosim numele corect al coloanei: capacitate
            stmt.setInt(7, room.getCapacity() != null ? room.getCapacity() : 0);

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
            logger.error("Eroare la salvarea camerei: " + room.getRoomNumber() + " în hotelul cu id-ul: " + room.getHotelId(), e);
        }

        return null;
    }

    public boolean update(Room room) {
        String sql = "UPDATE camera SET id_hotel = ?, nr_camera = ?, pret_per_noapte = ?, " +
                "id_poze = ?, facilitati = ?, tip_camera = ?, capacitate = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (room.getHotelId() != null) {
                stmt.setLong(1, room.getHotelId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }

            stmt.setString(2, room.getRoomNumber());
            stmt.setDouble(3, room.getPricePerNight());

            if (room.getImageId() != null) {
                stmt.setLong(4, room.getImageId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }

            stmt.setString(5, room.getAmenities());
            stmt.setString(6, room.getRoomType());

            // Actualizat: Folosim numele corect al coloanei: capacitate
            stmt.setInt(7, room.getCapacity() != null ? room.getCapacity() : 0);
            stmt.setLong(8, room.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Eroare la actualizarea camerei cu id-ul: " + room.getId(), e);
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM camera WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Eroare la ștergerea camerei cu id-ul: " + id, e);
            return false;
        }
    }

    public List<String> findAllRoomTypes() {
        List<String> types = new ArrayList<>();

        try {
            String sql = "SELECT DISTINCT tip_camera FROM camera WHERE tip_camera IS NOT NULL ORDER BY tip_camera";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String type = rs.getString("tip_camera");
                    if (type != null && !type.isEmpty()) {
                        types.add(type);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Eroare la obținerea tuturor tipurilor de camere", e);
            // Tip-uri predefinite în caz de eroare
            types.addAll(Arrays.asList("Single", "Double", "Twin", "Suite", "Deluxe"));
        }

        // Dacă nu s-au găsit tipuri în baza de date, adăugăm valori implicite
        if (types.isEmpty()) {
            types.addAll(Arrays.asList("Single", "Double", "Twin", "Suite", "Deluxe"));
        }

        return types;
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));

        try {
            Long hotelId = rs.getLong("id_hotel");
            if (!rs.wasNull()) {
                room.setHotelId(hotelId);
            }
        } catch (SQLException e) {
            // Câmpul ar putea să nu existe
        }

        try {
            room.setRoomNumber(rs.getString("nr_camera"));
        } catch (SQLException e) {
            // Câmpul ar putea să nu existe
        }

        try {
            room.setPricePerNight(rs.getDouble("pret_per_noapte"));
        } catch (SQLException e) {
            // Câmpul ar putea să nu existe
        }

        try {
            Long imageId = rs.getLong("id_poze");
            if (!rs.wasNull()) {
                room.setImageId(imageId);
            }
        } catch (SQLException e) {
            // Câmpul ar putea să nu existe
        }

        try {
            room.setAmenities(rs.getString("facilitati"));
        } catch (SQLException e) {
            // Câmpul ar putea să nu existe
        }

        try {
            room.setRoomType(rs.getString("tip_camera"));
        } catch (SQLException e) {
            // Câmpul ar putea să nu existe
        }

        try {
            // Actualizat: Folosim numele corect al coloanei: capacitate
            int capacitate = rs.getInt("capacitate");
            if (!rs.wasNull()) {
                room.setCapacity(capacitate);
            }
        } catch (SQLException e) {
            // Câmpul ar putea să nu existe
            logger.debug("Câmpul capacitate nu a fost găsit în rezultatul interogării", e);
        }

        return room;
    }
}