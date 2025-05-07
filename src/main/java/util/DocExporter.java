package util;

import model.Reservation;
import model.Room;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DocExporter {
    private static final Logger logger = LoggerFactory.getLogger(DocExporter.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public void exportReservationList(List<Reservation> reservations, File file) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(file)) {

            // Add title
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("RAPORT REZERVĂRI");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // Add date
            XWPFParagraph dateParagraph = document.createParagraph();
            dateParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun dateRun = dateParagraph.createRun();
            dateRun.setText("Data raport: " +
                    LocalDateTime.now().format(DATE_FORMATTER));

            // Add empty paragraph
            document.createParagraph();

            // Create table
            XWPFTable table = document.createTable();

            // Create header row
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("ID");
            headerRow.addNewTableCell().setText("Cameră");
            headerRow.addNewTableCell().setText("Hotel");
            headerRow.addNewTableCell().setText("Nume Client");
            headerRow.addNewTableCell().setText("Email");
            headerRow.addNewTableCell().setText("Telefon");
            headerRow.addNewTableCell().setText("Check-in");
            headerRow.addNewTableCell().setText("Check-out");
            headerRow.addNewTableCell().setText("Preț Total");
            headerRow.addNewTableCell().setText("Status Plată");

            // Format header row
            for (XWPFTableCell cell : headerRow.getTableCells()) {
                cell.setColor("DDDDDD");

                XWPFParagraph paragraph = cell.getParagraphs().get(0);
                paragraph.setAlignment(ParagraphAlignment.CENTER);

                XWPFRun run = paragraph.getRuns().get(0);
                run.setBold(true);
            }

            // Add data rows
            for (Reservation reservation : reservations) {
                if (reservation.getRoom() != null) {
                    XWPFTableRow dataRow = table.createRow();
                    dataRow.getCell(0).setText(reservation.getId().toString());
                    dataRow.getCell(1).setText(reservation.getRoom().getRoomNumber());
                    dataRow.getCell(2).setText(reservation.getRoom().getHotel() != null ?
                            reservation.getRoom().getHotel().getName() : "N/A");
                    dataRow.getCell(3).setText(reservation.getCustomerName());
                    dataRow.getCell(4).setText(reservation.getCustomerEmail());
                    dataRow.getCell(5).setText(reservation.getCustomerPhone());
                    dataRow.getCell(6).setText(reservation.getStartDate().format(DATE_FORMATTER));
                    dataRow.getCell(7).setText(reservation.getEndDate().format(DATE_FORMATTER));
                    dataRow.getCell(8).setText(reservation.getTotalPrice() != null ?
                            reservation.getTotalPrice().toString() : "N/A");
                    dataRow.getCell(9).setText(reservation.getPaymentStatus());
                }
            }

            // Add footer
            XWPFParagraph footerParagraph = document.createParagraph();
            footerParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun footerRun = footerParagraph.createRun();
            footerRun.setText("Total rezervări: " + reservations.size());

            document.write(out);
            logger.info("DOC export of reservations completed successfully to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error exporting reservations to DOC: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void exportRoomList(List<Room> rooms, File file) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(file)) {

            // Add title
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("RAPORT CAMERE DISPONIBILE");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // Add date
            XWPFParagraph dateParagraph = document.createParagraph();
            dateParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun dateRun = dateParagraph.createRun();
            dateRun.setText("Data raport: " +
                    LocalDateTime.now().format(DATE_FORMATTER));

            // Add empty paragraph
            document.createParagraph();

            // Create table
            XWPFTable table = document.createTable();

            // Create header row
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("ID");
            headerRow.addNewTableCell().setText("Număr Cameră");
            headerRow.addNewTableCell().setText("Hotel");
            headerRow.addNewTableCell().setText("Tip Cameră");
            headerRow.addNewTableCell().setText("Capacitate");
            headerRow.addNewTableCell().setText("Preț pe Noapte");
            headerRow.addNewTableCell().setText("Facilități");

            // Format header row
            for (XWPFTableCell cell : headerRow.getTableCells()) {
                cell.setColor("DDDDDD");

                XWPFParagraph paragraph = cell.getParagraphs().get(0);
                paragraph.setAlignment(ParagraphAlignment.CENTER);

                XWPFRun run = paragraph.getRuns().get(0);
                run.setBold(true);
            }

            // Add data rows
            for (Room room : rooms) {
                XWPFTableRow dataRow = table.createRow();
                dataRow.getCell(0).setText(room.getId().toString());
                dataRow.getCell(1).setText(room.getRoomNumber());
                dataRow.getCell(2).setText(room.getHotel() != null ? room.getHotel().getName() : "N/A");
                dataRow.getCell(3).setText(room.getRoomType());
                dataRow.getCell(4).setText(room.getCapacity() != null ? room.getCapacity().toString() : "N/A");
                dataRow.getCell(5).setText(room.getPricePerNight() != null ? room.getPricePerNight().toString() : "N/A");
                dataRow.getCell(6).setText(room.getAmenities());
            }

            // Add footer
            XWPFParagraph footerParagraph = document.createParagraph();
            footerParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun footerRun = footerParagraph.createRun();
            footerRun.setText("Total camere disponibile: " + rooms.size());

            document.write(out);
            logger.info("DOC export of rooms completed successfully to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error exporting rooms to DOC: {}", e.getMessage(), e);
            throw e;
        }
    }
}