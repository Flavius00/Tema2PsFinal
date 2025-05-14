package repository;

import model.Reservation;
import model.Room;
import model.Hotel;
import org.example.tema2ps.DBConnection;
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
        String sql = "SELECT r.*, c.nr_camera, c.pret_per_noapte, c.tip_camera, c.capacitate, " +
                "c.id_hotel, h.nume as hotel_name " +
                "FROM rezervari r " +
                "LEFT JOIN camera c ON r.id_camera = c.id " +
                "LEFT JOIN hotel h ON c.id_hotel = h.id " +
                "ORDER BY r.start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reservation reservation = mapResultSetToReservation(rs);
                reservations.add(reservation);
            }

            logger.info("Au fost găsite {} rezervări.", reservations.size());
        } catch (SQLException e) {
            logger.error("Eroare la preluarea tuturor rezervărilor", e);
        }

        return reservations;
    }

    public List<Reservation> findByRoomId(Long roomId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, c.nr_camera, c.pret_per_noapte, c.tip_camera, c.capacitate, " +
                "c.id_hotel, h.nume as hotel_name " +
                "FROM rezervari r " +
                "LEFT JOIN camera c ON r.id_camera = c.id " +
                "LEFT JOIN hotel h ON c.id_hotel = h.id " +
                "WHERE r.id_camera = ? ORDER BY r.start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    reservations.add(reservation);
                }
            }

            logger.info("Au fost găsite {} rezervări pentru camera cu id-ul {}.", reservations.size(), roomId);
        } catch (SQLException e) {
            logger.error("Eroare la preluarea rezervărilor după id-ul camerei: " + roomId, e);
        }

        return reservations;
    }

    public List<Reservation> findByHotelIdAndDate(Long hotelId, LocalDateTime date) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, c.nr_camera, c.pret_per_noapte, c.tip_camera, c.capacitate, " +
                "c.id_hotel, h.nume as hotel_name " +
                "FROM rezervari r " +
                "JOIN camera c ON r.id_camera = c.id " +
                "JOIN hotel h ON c.id_hotel = h.id " +
                "WHERE c.id_hotel = ? AND ? BETWEEN r.start_date AND r.end_date " +
                "ORDER BY r.start_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, hotelId);
            stmt.setTimestamp(2, Timestamp.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    reservations.add(reservation);
                }
            }

            logger.info("Au fost găsite {} rezervări pentru hotelul cu id-ul {} la data {}.",
                    reservations.size(), hotelId, date);
        } catch (SQLException e) {
            logger.error("Eroare la preluarea rezervărilor după id-ul hotelului și dată: " + hotelId, e);
        }

        return reservations;
    }

    public List<Reservation> findByHotelId(Long hotelId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, c.nr_camera, c.pret_per_noapte, c.tip_camera, c.capacitate, " +
                "c.id_hotel, h.nume as hotel_name " +
                "FROM rezervari r " +
                "JOIN camera c ON r.id_camera = c.id " +
                "JOIN hotel h ON c.id_hotel = h.id " +
                "WHERE c.id_hotel = ? " +
                "ORDER BY r.start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, hotelId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    reservations.add(reservation);
                }
            }

            logger.info("Au fost găsite {} rezervări pentru hotelul cu id-ul {}.",
                    reservations.size(), hotelId);
        } catch (SQLException e) {
            logger.error("Eroare la preluarea rezervărilor după id-ul hotelului: " + hotelId, e);
        }

        return reservations;
    }

    public List<Reservation> findByCustomerName(String customerName) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, c.nr_camera, c.pret_per_noapte, c.tip_camera, c.capacitate, " +
                "c.id_hotel, h.nume as hotel_name " +
                "FROM rezervari r " +
                "LEFT JOIN camera c ON r.id_camera = c.id " +
                "LEFT JOIN hotel h ON c.id_hotel = h.id " +
                "WHERE r.customer_name LIKE ? ORDER BY r.start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + customerName + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    reservations.add(reservation);
                }
            }

            logger.info("Au fost găsite {} rezervări pentru clientul {}.",
                    reservations.size(), customerName);
        } catch (SQLException e) {
            logger.error("Eroare la preluarea rezervărilor după numele clientului: " + customerName, e);
        }

        return reservations;
    }

    public Optional<Reservation> findById(Long id) {
        String sql = "SELECT r.*, c.nr_camera, c.pret_per_noapte, c.tip_camera, c.capacitate, " +
                "c.id_hotel, h.nume as hotel_name " +
                "FROM rezervari r " +
                "LEFT JOIN camera c ON r.id_camera = c.id " +
                "LEFT JOIN hotel h ON c.id_hotel = h.id " +
                "WHERE r.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    return Optional.of(reservation);
                }
            }
        } catch (SQLException e) {
            logger.error("Eroare la preluarea rezervării cu id-ul: " + id, e);
        }

        return Optional.empty();
    }

    public Long save(Reservation reservation) {
        String sql = "INSERT INTO rezervari (start_date, end_date, id_camera, customer_name, customer_email, customer_phone, total_price, payment_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String[] generatedColumns = {"id"};

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
            logger.error("Eroare la salvarea rezervării pentru camera cu id-ul: " + reservation.getRoomId(), e);
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
            logger.error("Eroare la actualizarea rezervării cu id-ul: " + reservation.getId(), e);
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
            logger.error("Eroare la ștergerea rezervării cu id-ul: " + id, e);
            return false;
        }
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();

        try {
            reservation.setId(rs.getLong("id"));
        } catch (SQLException e) {
            logger.error("Eroare la citirea id-ului rezervării", e);
        }

        try {
            Timestamp startTimestamp = rs.getTimestamp("start_date");
            if (startTimestamp != null) {
                reservation.setStartDate(startTimestamp.toLocalDateTime());
            }

            Timestamp endTimestamp = rs.getTimestamp("end_date");
            if (endTimestamp != null) {
                reservation.setEndDate(endTimestamp.toLocalDateTime());
            }
        } catch (SQLException e) {
            logger.error("Eroare la citirea datelor start_date sau end_date", e);
        }

        try {
            Long roomId = rs.getLong("id_camera");
            if (!rs.wasNull()) {
                reservation.setRoomId(roomId);

                // Creăm și obiectul Room din datele din rezultat
                Room room = new Room();
                room.setId(roomId);

                try {
                    room.setRoomNumber(rs.getString("nr_camera"));
                    room.setPricePerNight(rs.getDouble("pret_per_noapte"));
                    room.setRoomType(rs.getString("tip_camera"));

                    // Încercăm să obținem capacitatea
                    try {
                        int capacitate = rs.getInt("capacitate");
                        if (!rs.wasNull()) {
                            room.setCapacity(capacitate);
                        }
                    } catch (SQLException e) {
                        // Ignorăm eroarea dacă capacitatea nu există
                    }

                    // Încercăm să obținem hotelul
                    try {
                        Long hotelId = rs.getLong("id_hotel");
                        if (!rs.wasNull()) {
                            room.setHotelId(hotelId);

                            Hotel hotel = new Hotel();
                            hotel.setId(hotelId);
                            hotel.setName(rs.getString("hotel_name"));
                            room.setHotel(hotel);
                        }
                    } catch (SQLException e) {
                        // Ignorăm eroarea dacă id_hotel nu există
                    }

                    reservation.setRoom(room);
                } catch (SQLException e) {
                    // Ignorăm eroarea dacă unele câmpuri ale camerei nu există
                }
            }
        } catch (SQLException e) {
            logger.error("Eroare la citirea câmpului id_camera", e);
        }

        // Preluăm și restul câmpurilor din baza de date
        try {
            reservation.setCustomerName(rs.getString("customer_name"));
        } catch (SQLException e) {
            logger.debug("Câmpul customer_name ar putea să lipsească din tabelul rezervărilor");
        }

        try {
            reservation.setCustomerEmail(rs.getString("customer_email"));
        } catch (SQLException e) {
            logger.debug("Câmpul customer_email ar putea să lipsească din tabelul rezervărilor");
        }

        try {
            reservation.setCustomerPhone(rs.getString("customer_phone"));
        } catch (SQLException e) {
            logger.debug("Câmpul customer_phone ar putea să lipsească din tabelul rezervărilor");
        }

        try {
            Double totalPrice = rs.getDouble("total_price");
            if (!rs.wasNull()) {
                reservation.setTotalPrice(totalPrice);
            }
        } catch (SQLException e) {
            logger.debug("Câmpul total_price ar putea să lipsească din tabelul rezervărilor");
        }

        try {
            reservation.setPaymentStatus(rs.getString("payment_status"));
        } catch (SQLException e) {
            logger.debug("Câmpul payment_status ar putea să lipsească din tabelul rezervărilor");
        }

        return reservation;
    }
}