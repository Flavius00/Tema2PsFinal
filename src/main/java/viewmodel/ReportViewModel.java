package viewmodel;

import model.Hotel;
import service.HotelService;
import service.ReportService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ReportViewModel {
    private final HotelService hotelService;
    private final ReportService reportService;

    private final ObservableList<Hotel> hotels = FXCollections.observableArrayList();
    private final ObjectProperty<Hotel> selectedHotel = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> reportDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>(LocalDate.now().plusDays(1));

    private final StringProperty statusMessage = new SimpleStringProperty("");

    public ReportViewModel() {
        this.hotelService = new HotelService();
        this.reportService = new ReportService();
        loadHotels();
    }

    public void loadHotels() {
        List<Hotel> hotelList = hotelService.getAllHotels();
        hotels.setAll(hotelList);
    }

    public boolean exportReservationsToCsv(Stage stage) {
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            return false;
        }

        if (reportDate.get() == null) {
            statusMessage.set("Selectați o dată pentru raport");
            return false;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvare Raport CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        String hotelName = selectedHotel.get().getName().replaceAll("[^a-zA-Z0-9]", "_");
        String dateStr = reportDate.get().toString();
        fileChooser.setInitialFileName("rezervari_" + hotelName + "_" + dateStr + ".csv");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            LocalDateTime dateTime = reportDate.get().atTime(12, 0); // Noon of the selected date
            boolean success = reportService.exportReservationsToCsv(selectedHotel.get().getId(), dateTime, file);
            statusMessage.set(success ? "Raport CSV salvat cu succes" : "Eroare la salvarea raportului CSV");
            return success;
        }
        return false;
    }

    public boolean exportReservationsToDoc(Stage stage) {
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            return false;
        }

        if (reportDate.get() == null) {
            statusMessage.set("Selectați o dată pentru raport");
            return false;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvare Raport DOC");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DOC Files", "*.doc")
        );

        String hotelName = selectedHotel.get().getName().replaceAll("[^a-zA-Z0-9]", "_");
        String dateStr = reportDate.get().toString();
        fileChooser.setInitialFileName("rezervari_" + hotelName + "_" + dateStr + ".doc");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            LocalDateTime dateTime = reportDate.get().atTime(12, 0); // Noon of the selected date
            boolean success = reportService.exportReservationsToDoc(selectedHotel.get().getId(), dateTime, file);
            statusMessage.set(success ? "Raport DOC salvat cu succes" : "Eroare la salvarea raportului DOC");
            return success;
        }
        return false;
    }

    public boolean exportAvailableRoomsToCsv(Stage stage) {
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            return false;
        }

        if (startDate.get() == null || endDate.get() == null) {
            statusMessage.set("Selectați intervalul de date");
            return false;
        }

        if (startDate.get().isAfter(endDate.get())) {
            statusMessage.set("Data de început nu poate fi după data de sfârșit");
            return false;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvare Raport CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        String hotelName = selectedHotel.get().getName().replaceAll("[^a-zA-Z0-9]", "_");
        String dateStr = startDate.get().toString();
        fileChooser.setInitialFileName("camere_disponibile_" + hotelName + "_" + dateStr + ".csv");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            LocalDateTime startDateTime = startDate.get().atStartOfDay();
            LocalDateTime endDateTime = endDate.get().atTime(LocalTime.MAX);
            boolean success = reportService.exportAvailableRoomsToCsv(selectedHotel.get().getId(), startDateTime, endDateTime, file);
            statusMessage.set(success ? "Raport CSV salvat cu succes" : "Eroare la salvarea raportului CSV");
            return success;
        }
        return false;
    }

    public boolean exportAvailableRoomsToDoc(Stage stage) {
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            return false;
        }

        if (startDate.get() == null || endDate.get() == null) {
            statusMessage.set("Selectați intervalul de date");
            return false;
        }

        if (startDate.get().isAfter(endDate.get())) {
            statusMessage.set("Data de început nu poate fi după data de sfârșit");
            return false;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvare Raport DOC");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DOC Files", "*.doc")
        );

        String hotelName = selectedHotel.get().getName().replaceAll("[^a-zA-Z0-9]", "_");
        String dateStr = startDate.get().toString();
        fileChooser.setInitialFileName("camere_disponibile_" + hotelName + "_" + dateStr + ".doc");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            LocalDateTime startDateTime = startDate.get().atStartOfDay();
            LocalDateTime endDateTime = endDate.get().atTime(LocalTime.MAX);
            boolean success = reportService.exportAvailableRoomsToDoc(selectedHotel.get().getId(), startDateTime, endDateTime, file);
            statusMessage.set(success ? "Raport DOC salvat cu succes" : "Eroare la salvarea raportului DOC");
            return success;
        }
        return false;
    }

    // Getters for observable properties
    public ObservableList<Hotel> getHotels() {
        return hotels;
    }

    public ObjectProperty<Hotel> selectedHotelProperty() {
        return selectedHotel;
    }

    public ObjectProperty<LocalDate> reportDateProperty() {
        return reportDate;
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
}