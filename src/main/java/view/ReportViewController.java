package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Hotel;
import viewmodel.ReportViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportViewController {
    private static final Logger logger = LoggerFactory.getLogger(ReportViewController.class);

    private ReportViewModel viewModel;

    @FXML
    private ComboBox<Hotel> hotelReservationsComboBox;

    @FXML
    private ComboBox<Hotel> hotelRoomsComboBox;

    @FXML
    private DatePicker reservationDatePicker;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button exportReservationsCsvButton;

    @FXML
    private Button exportReservationsDocButton;

    @FXML
    private Button exportRoomsCsvButton;

    @FXML
    private Button exportRoomsDocButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea instructionsTextArea;

    @FXML
    private void initialize() {
        viewModel = new ReportViewModel();
        logger.info("Inițializare ReportViewController");

        // Setup bindings for form fields
        reservationDatePicker.valueProperty().bindBidirectional(viewModel.reportDateProperty());
        startDatePicker.valueProperty().bindBidirectional(viewModel.startDateProperty());
        endDatePicker.valueProperty().bindBidirectional(viewModel.endDateProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // Setup hotel combo boxes with common converter
        StringConverter<Hotel> hotelConverter = new StringConverter<Hotel>() {
            @Override
            public String toString(Hotel hotel) {
                return hotel == null ? "" : hotel.getName();
            }

            @Override
            public Hotel fromString(String string) {
                return null; // Not needed for combo box
            }
        };

        hotelReservationsComboBox.setItems(viewModel.getHotels());
        hotelReservationsComboBox.valueProperty().bindBidirectional(viewModel.selectedHotelProperty());
        hotelReservationsComboBox.setConverter(hotelConverter);

        hotelRoomsComboBox.setItems(viewModel.getHotels());
        hotelRoomsComboBox.valueProperty().bindBidirectional(viewModel.selectedHotelProperty());
        hotelRoomsComboBox.setConverter(hotelConverter);

        // Synchronize hotel selection between combo boxes
        hotelReservationsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(hotelRoomsComboBox.getValue())) {
                hotelRoomsComboBox.setValue(newVal);
            }
        });

        hotelRoomsComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(hotelReservationsComboBox.getValue())) {
                hotelReservationsComboBox.setValue(newVal);
            }
        });

        // Setăm stage-ul în viewModel pentru a permite deschiderea dialog-urilor
        // Acest lucru trebuie făcut după ce componenta este adăugată la scenă
        // Adăugăm un listener pentru a apela setStage atunci când componenta este vizibilă
        statusLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Apelăm acest cod după ce scena este creată și atașată la o fereastră
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        viewModel.setStage((Stage) newWindow);
                        logger.info("Stage setat în ReportViewModel");
                    }
                });
            }
        });

        // Setup action bindings pentru butoane
        exportReservationsCsvButton.disableProperty().bind(
                viewModel.selectedHotelProperty().isNull().or(viewModel.reportDateProperty().isNull())
        );
        exportReservationsCsvButton.onActionProperty().bind(viewModel.exportReservationsCsvActionProperty());

        exportReservationsDocButton.disableProperty().bind(
                viewModel.selectedHotelProperty().isNull().or(viewModel.reportDateProperty().isNull())
        );
        exportReservationsDocButton.onActionProperty().bind(viewModel.exportReservationsDocActionProperty());

        exportRoomsCsvButton.disableProperty().bind(
                viewModel.selectedHotelProperty().isNull()
                        .or(viewModel.startDateProperty().isNull())
                        .or(viewModel.endDateProperty().isNull())
        );
        exportRoomsCsvButton.onActionProperty().bind(viewModel.exportRoomsCsvActionProperty());

        exportRoomsDocButton.disableProperty().bind(
                viewModel.selectedHotelProperty().isNull()
                        .or(viewModel.startDateProperty().isNull())
                        .or(viewModel.endDateProperty().isNull())
        );
        exportRoomsDocButton.onActionProperty().bind(viewModel.exportRoomsDocActionProperty());

        logger.info("Inițializare ReportViewController finalizată");
    }
}