package service;

import model.Reservation;
import model.Room;
import repository.ReservationRepository;
import repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservationService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public ReservationService() {
        this.reservationRepository = new ReservationRepository();
        this.roomRepository = new RoomRepository();
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        logger.info("Au fost preluate {} rezervări din baza de date", reservations.size());
        return reservations;
    }

    public List<Reservation> getReservationsByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    public List<Reservation> getReservationsByHotelIdAndDate(Long hotelId, LocalDateTime date) {
        return reservationRepository.findByHotelIdAndDate(hotelId, date);
    }

    public List<Reservation> getReservationsByHotelId(Long hotelId) {
        return reservationRepository.findByHotelId(hotelId);
    }

    public List<Reservation> getReservationsByCustomerName(String customerName) {
        return reservationRepository.findByCustomerName(customerName);
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public boolean addReservation(Reservation reservation) {
        try {
            // Verify the room exists and is available
            Optional<Room> roomOpt = roomRepository.findById(reservation.getRoomId());
            if (!roomOpt.isPresent()) {
                logger.error("Nu se poate adăuga rezervarea: Camera cu id-ul {} nu a fost găsită", reservation.getRoomId());
                return false;
            }

            Room room = roomOpt.get();

            // Check if the room is available for the requested dates
            List<Reservation> existingReservations = reservationRepository.findByRoomId(room.getId());
            for (Reservation existingRes : existingReservations) {
                // If there's any overlap with existing reservations, the room is not available
                if (!(reservation.getEndDate().isBefore(existingRes.getStartDate()) ||
                        reservation.getStartDate().isAfter(existingRes.getEndDate()))) {
                    logger.error("Nu se poate adăuga rezervarea: Camera {} nu este disponibilă în perioada solicitată", room.getRoomNumber());
                    return false;
                }
            }

            // Calculate total price
            if (reservation.getTotalPrice() == null || reservation.getTotalPrice() <= 0) {
                reservation.setRoom(room);
                reservation.calculateTotalPrice();
            }

            // Set default payment status if not provided
            if (reservation.getPaymentStatus() == null || reservation.getPaymentStatus().isEmpty()) {
                reservation.setPaymentStatus("Pending");
            }

            Long id = reservationRepository.save(reservation);
            if (id != null) {
                reservation.setId(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Eroare la adăugarea rezervării pentru camera cu id-ul: {}", reservation.getRoomId(), e);
            return false;
        }
    }

    public boolean updateReservation(Reservation reservation) {
        try {
            // Verify the room exists
            Optional<Room> roomOpt = roomRepository.findById(reservation.getRoomId());
            if (!roomOpt.isPresent()) {
                logger.error("Nu se poate actualiza rezervarea: Camera cu id-ul {} nu a fost găsită", reservation.getRoomId());
                return false;
            }

            // For updates, we need to check availability excluding the current reservation
            Room room = roomOpt.get();

            // Get the existing reservation to compare dates
            Optional<Reservation> existingResOpt = reservationRepository.findById(reservation.getId());
            if (!existingResOpt.isPresent()) {
                logger.error("Nu se poate actualiza rezervarea: Rezervarea cu id-ul {} nu a fost găsită", reservation.getId());
                return false;
            }

            // Check if dates have changed
            Reservation existingRes = existingResOpt.get();
            if (!existingRes.getStartDate().equals(reservation.getStartDate()) ||
                    !existingRes.getEndDate().equals(reservation.getEndDate())) {

                // Check for conflicts with other reservations
                List<Reservation> reservations = reservationRepository.findByRoomId(room.getId());
                for (Reservation res : reservations) {
                    // Skip the current reservation being updated
                    if (res.getId().equals(reservation.getId())) {
                        continue;
                    }

                    // If there's any overlap with other reservations, the room is not available
                    if (!(reservation.getEndDate().isBefore(res.getStartDate()) ||
                            reservation.getStartDate().isAfter(res.getEndDate()))) {
                        logger.error("Nu se poate actualiza rezervarea: Camera {} nu este disponibilă în perioada solicitată", room.getRoomNumber());
                        return false;
                    }
                }
            }

            // Update price if room or dates have changed
            if (!existingRes.getRoomId().equals(reservation.getRoomId()) ||
                    !existingRes.getStartDate().equals(reservation.getStartDate()) ||
                    !existingRes.getEndDate().equals(reservation.getEndDate())) {

                reservation.setRoom(room);
                reservation.calculateTotalPrice();
            }

            return reservationRepository.update(reservation);
        } catch (Exception e) {
            logger.error("Eroare la actualizarea rezervării cu id-ul: {}", reservation.getId(), e);
            return false;
        }
    }

    public boolean deleteReservation(Long id) {
        try {
            return reservationRepository.delete(id);
        } catch (Exception e) {
            logger.error("Eroare la ștergerea rezervării cu id-ul: {}", id, e);
            return false;
        }
    }
}