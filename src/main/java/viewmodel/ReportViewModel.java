package viewmodel;

import model.Hotel;
import service.HotelService;
import service.ReportService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ReportViewModel {
    private static final Logger logger = LoggerFactory.getLogger(ReportViewModel.class);
    private final HotelService hotelService;
    private final ReportService reportService;

    private final ObservableList<Hotel> hotels = FXCollections.observableArrayList();
    private final ObjectProperty<Hotel> selectedHotel = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> reportDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>(LocalDate.now().plusDays(1));

    private final StringProperty statusMessage = new SimpleStringProperty("");

    // Stage pentru file chooser dialogs
    private Stage stage;

    // Action properties pentru butoane
    private final ObjectProperty<EventHandler<ActionEvent>> exportReservationsCsvAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> exportReservationsDocAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> exportRoomsCsvAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> exportRoomsDocAction = new SimpleObjectProperty<>();

    public ReportViewModel() {
        this.hotelService = new HotelService();
        this.reportService = new ReportService();
        loadHotels();

        // Setăm acțiunile pentru butoane
        exportReservationsCsvAction.set(event -> exportReservationsToCsv());
        exportReservationsDocAction.set(event -> exportReservationsToDoc());
        exportRoomsCsvAction.set(event -> exportAvailableRoomsToCsv());
        exportRoomsDocAction.set(event -> exportAvailableRoomsToDoc());

        logger.info("ReportViewModel inițializat");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        logger.info("Stage setat în ReportViewModel: {}", stage);
    }

    public void loadHotels() {
        List<Hotel> hotelList = hotelService.getAllHotels();
        hotels.setAll(hotelList);
        logger.info("Au fost încărcate {} hoteluri", hotelList.size());
    }

    /**
     * Helper method to check if stage is set and show alert if not
     * @return true if stage is set, false otherwise
     */
    private boolean checkStage() {
        if (stage == null) {
            logger.error("Stage nesetat în ReportViewModel");
            statusMessage.set("Eroare: Stage nesetat. Reîncărcați pagina.");

            // Show an alert dialog to notify the user
            try {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Eroare");
                alert.setHeaderText("Stage nesetat");
                alert.setContentText("A apărut o eroare la inițializarea ferestrei de export. Vă rugăm să reîncărcați pagina și să încercați din nou.");
                alert.showAndWait();
            } catch (Exception e) {
                logger.error("Nu s-a putut afișa alerta: {}", e.getMessage());
            }

            return false;
        }
        return true;
    }

    public boolean exportReservationsToCsv() {
        logger.info("Inițiere export rezervări în CSV");
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            logger.warn("Export rezervări în CSV eșuat: Hotel neselectat");
            return false;
        }

        if (reportDate.get() == null) {
            statusMessage.set("Selectați o dată pentru raport");
            logger.warn("Export rezervări în CSV eșuat: Dată neselectată");
            return false;
        }

        if (!checkStage()) {
            return false;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvare Raport Rezervări CSV");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            String hotelName = selectedHotel.get().getName().replaceAll("[^a-zA-Z0-9]", "_");
            String dateStr = reportDate.get().toString();
            fileChooser.setInitialFileName("rezervari_" + hotelName + "_" + dateStr + ".csv");

            logger.info("Deschidere dialog FileChooser pentru CSV");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                logger.info("Fișier selectat pentru salvare: {}", file.getAbsolutePath());
                LocalDateTime dateTime = reportDate.get().atTime(12, 0); // Noon of the selected date
                boolean success = reportService.exportReservationsToCsv(selectedHotel.get().getId(), dateTime, file);
                statusMessage.set(success ? "Raport CSV salvat cu succes" : "Eroare la salvarea raportului CSV");
                logger.info("Export rezervări în CSV: {}", success ? "Succes" : "Eșuat");
                return success;
            } else {
                logger.info("Export CSV anulat de utilizator");
            }
        } catch (Exception e) {
            statusMessage.set("Eroare la exportul în CSV: " + e.getMessage());
            logger.error("Excepție la exportul rezervărilor în CSV", e);
        }
        return false;
    }

    public boolean exportReservationsToDoc() {
        logger.info("Inițiere export rezervări în DOC");
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            logger.warn("Export rezervări în DOC eșuat: Hotel neselectat");
            return false;
        }

        if (reportDate.get() == null) {
            statusMessage.set("Selectați o dată pentru raport");
            logger.warn("Export rezervări în DOC eșuat: Dată neselectată");
            return false;
        }

        if (!checkStage()) {
            return false;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvare Raport Rezervări DOC");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("DOC Files", "*.doc")
            );

            String hotelName = selectedHotel.get().getName().replaceAll("[^a-zA-Z0-9]", "_");
            String dateStr = reportDate.get().toString();
            fileChooser.setInitialFileName("rezervari_" + hotelName + "_" + dateStr + ".doc");

            logger.info("Deschidere dialog FileChooser pentru DOC");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                logger.info("Fișier selectat pentru salvare: {}", file.getAbsolutePath());
                LocalDateTime dateTime = reportDate.get().atTime(12, 0); // Noon of the selected date
                boolean success = reportService.exportReservationsToDoc(selectedHotel.get().getId(), dateTime, file);
                statusMessage.set(success ? "Raport DOC salvat cu succes" : "Eroare la salvarea raportului DOC");
                logger.info("Export rezervări în DOC: {}", success ? "Succes" : "Eșuat");
                return success;
            } else {
                logger.info("Export DOC anulat de utilizator");
            }
        } catch (Exception e) {
            statusMessage.set("Eroare la exportul în DOC: " + e.getMessage());
            logger.error("Excepție la exportul rezervărilor în DOC", e);
        }
        return false;
    }

    public boolean exportAvailableRoomsToCsv() {
        logger.info("Inițiere export camere disponibile în CSV");
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            logger.warn("Export camere în CSV eșuat: Hotel neselectat");
            return false;
        }

        if (startDate.get() == null || endDate.get() == null) {
            statusMessage.set("Selectați intervalul de date");
            logger.warn("Export camere în CSV eșuat: Interval de date neselectat");
            return false;
        }

        if (startDate.get().isAfter(endDate.get())) {
            statusMessage.set("Data de început nu poate fi după data de sfârșit");
            logger.warn("Export camere în CSV eșuat: Interval de date invalid");
            return false;
        }

        if (!checkStage()) {
            return false;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvare Raport Camere Disponibile CSV");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            String hotelName = selectedHotel.get().getName().replaceAll("[^a-zA-Z0-9]", "_");
            String dateStr = startDate.get().toString();
            fileChooser.setInitialFileName("camere_disponibile_" + hotelName + "_" + dateStr + ".csv");

            logger.info("Deschidere dialog FileChooser pentru CSV camere");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                logger.info("Fișier selectat pentru salvare: {}", file.getAbsolutePath());
                LocalDateTime startDateTime = startDate.get().atStartOfDay();
                LocalDateTime endDateTime = endDate.get().atTime(LocalTime.MAX);
                boolean success = reportService.exportAvailableRoomsToCsv(selectedHotel.get().getId(), startDateTime, endDateTime, file);
                statusMessage.set(success ? "Raport CSV salvat cu succes" : "Eroare la salvarea raportului CSV");
                logger.info("Export camere disponibile în CSV: {}", success ? "Succes" : "Eșuat");
                return success;
            } else {
                logger.info("Export CSV camere anulat de utilizator");
            }
        } catch (Exception e) {
            statusMessage.set("Eroare la exportul în CSV: " + e.getMessage());
            logger.error("Excepție la exportul camerelor în CSV", e);
        }
        return false;
    }

    public boolean exportAvailableRoomsToDoc() {
        logger.info("Inițiere export camere disponibile în DOC");
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            logger.warn("Export camere în DOC eșuat: Hotel neselectat");
            return false;
        }

        if (startDate.get() == null || endDate.get() == null) {
            statusMessage.set("Selectați intervalul de date");
            logger.warn("Export camere în DOC eșuat: Interval de date neselectat");
            return false;
        }

        if (startDate.get().isAfter(endDate.get())) {
            statusMessage.set("Data de început nu poate fi după data de sfârșit");
            logger.warn("Export camere în DOC eșuat: Interval de date invalid");
            return false;
        }

        if (!checkStage()) {
            return false;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvare Raport Camere Disponibile DOC");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("DOC Files", "*.doc")
            );

            String hotelName = selectedHotel.get().getName().replaceAll("[^a-zA-Z0-9]", "_");
            String dateStr = startDate.get().toString();
            fileChooser.setInitialFileName("camere_disponibile_" + hotelName + "_" + dateStr + ".doc");

            logger.info("Deschidere dialog FileChooser pentru DOC camere");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                logger.info("Fișier selectat pentru salvare: {}", file.getAbsolutePath());
                LocalDateTime startDateTime = startDate.get().atStartOfDay();
                LocalDateTime endDateTime = endDate.get().atTime(LocalTime.MAX);
                boolean success = reportService.exportAvailableRoomsToDoc(selectedHotel.get().getId(), startDateTime, endDateTime, file);
                statusMessage.set(success ? "Raport DOC salvat cu succes" : "Eroare la salvarea raportului DOC");
                logger.info("Export camere disponibile în DOC: {}", success ? "Succes" : "Eșuat");
                return success;
            } else {
                logger.info("Export DOC camere anulat de utilizator");
            }
        } catch (Exception e) {
            statusMessage.set("Eroare la exportul în DOC: " + e.getMessage());
            logger.error("Excepție la exportul camerelor în DOC", e);
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

    // Getters for action properties
    public ObjectProperty<EventHandler<ActionEvent>> exportReservationsCsvActionProperty() {
        return exportReservationsCsvAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> exportReservationsDocActionProperty() {
        return exportReservationsDocAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> exportRoomsCsvActionProperty() {
        return exportRoomsCsvAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> exportRoomsDocActionProperty() {
        return exportRoomsDocAction;
    }
}