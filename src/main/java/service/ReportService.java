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
            List<Reservation> reservations = reservationService.getReservationsByHotelIdAndDate(hotelId, date);
            csvExporter.exportReservationList(reservations, file);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting reservations to CSV", e);
            return false;
        }
    }

    public boolean exportReservationsToDoc(Long hotelId, LocalDateTime date, File file) {
        try {
            List<Reservation> reservations = reservationService.getReservationsByHotelIdAndDate(hotelId, date);
            docExporter.exportReservationList(reservations, file);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting reservations to DOC", e);
            return false;
        }
    }

    public boolean exportAvailableRoomsToCsv(Long hotelId, LocalDateTime startDate, LocalDateTime endDate, File file) {
        try {
            List<Room> availableRooms = roomService.getAvailableRoomsByHotelIdAndDate(hotelId, startDate, endDate);
            csvExporter.exportRoomList(availableRooms, file);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting available rooms to CSV", e);
            return false;
        }
    }

    public boolean exportAvailableRoomsToDoc(Long hotelId, LocalDateTime startDate, LocalDateTime endDate, File file) {
        try {
            List<Room> availableRooms = roomService.getAvailableRoomsByHotelIdAndDate(hotelId, startDate, endDate);
            docExporter.exportRoomList(availableRooms, file);
            return true;
        } catch (IOException e) {
            logger.error("Error exporting available rooms to DOC", e);
            return false;
        }
    }
}