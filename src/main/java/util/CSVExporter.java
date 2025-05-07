package util;

import model.Reservation;
import model.Room;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {
    private static final Logger logger = LoggerFactory.getLogger(CSVExporter.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public void exportReservationList(List<Reservation> reservations, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                     .withHeader("ID", "Room Number", "Hotel", "Customer Name", "Email", "Phone",
                             "Check-in Date", "Check-out Date", "Total Price", "Payment Status"))) {

            for (Reservation reservation : reservations) {
                if (reservation.getRoom() != null) {
                    csvPrinter.printRecord(
                            reservation.getId(),
                            reservation.getRoom().getRoomNumber(),
                            reservation.getRoom().getHotel() != null ? reservation.getRoom().getHotel().getName() : "N/A",
                            reservation.getCustomerName(),
                            reservation.getCustomerEmail(),
                            reservation.getCustomerPhone(),
                            reservation.getStartDate().format(DATE_FORMATTER),
                            reservation.getEndDate().format(DATE_FORMATTER),
                            reservation.getTotalPrice(),
                            reservation.getPaymentStatus()
                    );
                }
            }

            csvPrinter.flush();
            logger.info("CSV export of reservations completed successfully to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error exporting reservations to CSV: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void exportRoomList(List<Room> rooms, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                     .withHeader("ID", "Room Number", "Hotel", "Room Type", "Capacity",
                             "Price per Night", "Amenities"))) {

            for (Room room : rooms) {
                csvPrinter.printRecord(
                        room.getId(),
                        room.getRoomNumber(),
                        room.getHotel() != null ? room.getHotel().getName() : "N/A",
                        room.getRoomType(),
                        room.getCapacity(),
                        room.getPricePerNight(),
                        room.getAmenities()
                );
            }

            csvPrinter.flush();
            logger.info("CSV export of rooms completed successfully to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error exporting rooms to CSV: {}", e.getMessage(), e);
            throw e;
        }
    }
}