package repository;

import model.Reservation;
import model.Room;
import org.example.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationRepository {
    private static final Logger logger = LoggerFactory.getLogger(ReservationRepository.class);
    private final RoomRepository roomRepository;

    public ReservationRepository() {
        this.roomRepository = new RoomRepository();
    }

    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM rezervari ORDER BY start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reservation reservation = mapResultSetToReservation(rs);

                // Load room data
                if (reservation.getRoomId() != null) {
                    Optional<Room> room = roomRepository.findById(reservation.getRoomId());
                    room.ifPresent(reservation::setRoom);
                }

                reservations.add(reservation);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all reservations", e);
        }

        return reservations;
    }

    public List<Reservation> findByRoomId(Long roomId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM rezervari WHERE id_camera = ? ORDER BY start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);

                    // Load room data
                    Optional<Room> room = roomRepository.findById(roomId);
                    room.ifPresent(reservation::setRoom);

                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching reservations by room id: " + roomId, e);
        }

        return reservations;
    }

    public List<Reservation> findByHotelIdAndDate(Long hotelId, LocalDateTime date) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.* FROM rezervari r " +
                "JOIN camera c ON r.id_camera = c.id " +
                "WHERE c.id_hotel = ? AND ? BETWEEN r.start_date AND r.end_date " +
                "ORDER BY r.start_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, hotelId);
            stmt.setTimestamp(2, Timestamp.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);

                    // Load room data
                    if (reservation.getRoomId() != null) {
                        Optional<Room> room = roomRepository.findById(reservation.getRoomId());
                        room.ifPresent(reservation::setRoom);
                    }

                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching reservations by hotel id and date: " + hotelId, e);
        }

        return reservations;
    }

    public List<Reservation> findByCustomerName(String customerName) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM rezervari WHERE customer_name LIKE ? ORDER BY start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + customerName + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);

                    // Load room data
                    if (reservation.getRoomId() != null) {
                        Optional<Room> room = roomRepository.findById(reservation.getRoomId());
                        room.ifPresent(reservation::setRoom);
                    }

                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching reservations by customer name: " + customerName, e);
        }

        return reservations;
    }

    public Optional<Reservation> findById(Long id) {
        String sql = "SELECT * FROM rezervari WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);

                    // Load room data
                    if (reservation.getRoomId() != null) {
                        Optional<Room> room = roomRepository.findById(reservation.getRoomId());
                        room.ifPresent(reservation::setRoom);
                    }

                    return Optional.of(reservation);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching reservation with id: " + id, e);
        }

        return Optional.empty();
    }

    public Long save(Reservation reservation) {
        String sql = "INSERT INTO rezervari (start_date, end_date, id_camera, customer_name, customer_email, customer_phone, total_price, payment_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(reservation.getStartDate()));
            stmt.setTimestamp(2, Timestamp.valueOf(reservation.getEndDate()));

            if (reservation.getRoomId() != null) {
                stmt.setLong(3, reservation.getRoomId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.setString(4, reservation.getCustomerName());
            stmt.setString(5, reservation.getCustomerEmail());
            stmt.setString(6, reservation.getCustomerPhone());

            if (reservation.getTotalPrice() != null) {
                stmt.setDouble(7, reservation.getTotalPrice());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }

            stmt.setString(8, reservation.getPaymentStatus());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving reservation for room id: " + reservation.getRoomId(), e);
        }

        return null;
    }

    public boolean update(Reservation reservation) {
        String sql = "UPDATE rezervari SET start_date = ?, end_date = ?, id_camera = ?, " +
                "customer_name = ?, customer_email = ?, customer_phone = ?, " +
                "total_price = ?, payment_status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(reservation.getStartDate()));
            stmt.setTimestamp(2, Timestamp.valueOf(reservation.getEndDate()));

            if (reservation.getRoomId() != null) {
                stmt.setLong(3, reservation.getRoomId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.setString(4, reservation.getCustomerName());
            stmt.setString(5, reservation.getCustomerEmail());
            stmt.setString(6, reservation.getCustomerPhone());

            if (reservation.getTotalPrice() != null) {
                stmt.setDouble(7, reservation.getTotalPrice());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }

            stmt.setString(8, reservation.getPaymentStatus());
            stmt.setLong(9, reservation.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating reservation with id: " + reservation.getId(), e);
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM rezervari WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting reservation with id: " + id, e);
            return false;
        }
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getLong("id"));
        reservation.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        reservation.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());

        Long roomId = rs.getLong("id_camera");
        if (!rs.wasNull()) {
            reservation.setRoomId(roomId);
        }

        // Get additional fields if they exist in the table
        try {
            reservation.setCustomerName(rs.getString("customer_name"));
            reservation.setCustomerEmail(rs.getString("customer_email"));
            reservation.setCustomerPhone(rs.getString("customer_phone"));

            Double totalPrice = rs.getDouble("total_price");
            if (!rs.wasNull()) {
                reservation.setTotalPrice(totalPrice);
            }

            reservation.setPaymentStatus(rs.getString("payment_status"));
        } catch (SQLException e) {
            // Fields might not exist in the database
            logger.debug("Some fields are missing in the reservation table", e);
        }

        return reservation;
    }
}