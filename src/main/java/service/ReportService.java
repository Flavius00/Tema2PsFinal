package service;

import model.Reservation;
import model.Room;
import util.CSVExporter;
import util.DocExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final CSVExporter csvExporter;
    private final DocExporter docExporter;

    public ReportService() {
        this.reservationService = new ReservationService();
        this.roomService = new RoomService();
        this.csvExporter = new CSVExporter();
        this.docExporter = new DocExporter();
    }

    public boolean exportReservationsToCsv(Long hotelId, LocalDateTime date, File file) {
        try {
            logger.info("Începere export rezervări în CSV pentru hotelul {} la data {}", hotelId, date);
            List<Reservation> reservations = reservationService.getReservationsByHotelIdAndDate(hotelId, date);
            logger.info("Au fost găsite {} rezervări pentru export", reservations.size());
            csvExporter.exportReservationList(reservations, file);
            logger.info("Export rezervări în CSV finalizat cu succes: {}", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Eroare la exportul rezervărilor în CSV", e);
            return false;
        }
    }

    public boolean exportReservationsToDoc(Long hotelId, LocalDateTime date, File file) {
        try {
            logger.info("Începere export rezervări în DOC pentru hotelul {} la data {}", hotelId, date);
            List<Reservation> reservations = reservationService.getReservationsByHotelIdAndDate(hotelId, date);
            logger.info("Au fost găsite {} rezervări pentru export", reservations.size());
            docExporter.exportReservationList(reservations, file);
            logger.info("Export rezervări în DOC finalizat cu succes: {}", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Eroare la exportul rezervărilor în DOC", e);
            return false;
        }
    }

    public boolean exportAvailableRoomsToCsv(Long hotelId, LocalDateTime startDate, LocalDateTime endDate, File file) {
        try {
            logger.info("Începere export camere disponibile în CSV pentru hotelul {} în perioada {} - {}",
                    hotelId, startDate, endDate);
            List<Room> availableRooms = roomService.getAvailableRoomsByHotelIdAndDate(hotelId, startDate, endDate);
            logger.info("Au fost găsite {} camere disponibile pentru export", availableRooms.size());
            csvExporter.exportRoomList(availableRooms, file);
            logger.info("Export camere disponibile în CSV finalizat cu succes: {}", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Eroare la exportul camerelor disponibile în CSV", e);
            return false;
        }
    }

    public boolean exportAvailableRoomsToDoc(Long hotelId, LocalDateTime startDate, LocalDateTime endDate, File file) {
        try {
            logger.info("Începere export camere disponibile în DOC pentru hotelul {} în perioada {} - {}",
                    hotelId, startDate, endDate);
            List<Room> availableRooms = roomService.getAvailableRoomsByHotelIdAndDate(hotelId, startDate, endDate);
            logger.info("Au fost găsite {} camere disponibile pentru export", availableRooms.size());
            docExporter.exportRoomList(availableRooms, file);
            logger.info("Export camere disponibile în DOC finalizat cu succes: {}", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Eroare la exportul camerelor disponibile în DOC", e);
            return false;
        }
    }
}