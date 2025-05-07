package repository;

import model.Hotel;
import model.Room;
import org.example.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import java.sql.*;
import java.util.ArrayList;
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
            logger.error("Error fetching rooms by hotel id: " + hotelId, e);
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
            logger.error("Error fetching rooms by hotel id and price range: " + hotelId, e);
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
            logger.error("Error fetching available rooms by hotel id and date: " + hotelId, e);
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
            logger.error("Error fetching room with id: " + id, e);
        }

        return Optional.empty();
    }

    public Long save(Room room) {
        String sql = "INSERT INTO camera (id_hotel, nr_camera, pret_per_noapte, id_poze, amenities, room_type, capacity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

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
            stmt.setInt(7, room.getCapacity());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving room: " + room.getRoomNumber() + " in hotel id: " + room.getHotelId(), e);
        }

        return null;
    }

    public boolean update(Room room) {
        String sql = "UPDATE camera SET id_hotel = ?, nr_camera = ?, pret_per_noapte = ?, " +
                "id_poze = ?, amenities = ?, room_type = ?, capacity = ? WHERE id = ?";

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
            stmt.setInt(7, room.getCapacity());
            stmt.setLong(8, room.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating room with id: " + room.getId(), e);
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
            logger.error("Error deleting room with id: " + id, e);
            return false;
        }
    }

    public List<String> findAllRoomTypes() {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT room_type FROM camera ORDER BY room_type";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String type = rs.getString("room_type");
                if (type != null && !type.isEmpty()) {
                    types.add(type);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching all room types", e);
        }

        return types;
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));

        Long hotelId = rs.getLong("id_hotel");
        if (!rs.wasNull()) {
            room.setHotelId(hotelId);
        }

        room.setRoomNumber(rs.getString("nr_camera"));
        room.setPricePerNight(rs.getDouble("pret_per_noapte"));

        Long imageId = rs.getLong("id_poze");
        if (!rs.wasNull()) {
            room.setImageId(imageId);
        }

        // Get additional fields if they exist in the table
        try {
            room.setAmenities(rs.getString("amenities"));
        } catch (SQLException e) {
            // Field might not exist in the database
        }

        try {
            room.setRoomType(rs.getString("room_type"));
        } catch (SQLException e) {
            // Field might not exist in the database
        }

        try {
            room.setCapacity(rs.getInt("capacity"));
        } catch (SQLException e) {
            // Field might not exist in the database
        }

        return room;
    }
}