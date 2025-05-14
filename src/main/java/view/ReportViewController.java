package view;

import javafx.application.Platform;
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

        // Important: Add a more reliable way to set the stage
        Platform.runLater(this::setStageInViewModel);

        logger.info("Inițializare ReportViewController finalizată");
    }

    /**
     * Method to set the stage in the viewModel
     * This will be called after the scene is fully initialized
     */
    private void setStageInViewModel() {
        try {
            if (exportReservationsCsvButton.getScene() != null &&
                    exportReservationsCsvButton.getScene().getWindow() != null) {
                Stage stage = (Stage) exportReservationsCsvButton.getScene().getWindow();
                viewModel.setStage(stage);
                logger.info("Stage setat cu succes în ReportViewModel: {}", stage);
            } else {
                logger.error("Nu s-a putut obține stage-ul pentru ReportViewModel - scene sau window null");
                // Try again later
                Platform.runLater(() -> {
                    if (exportReservationsCsvButton.getScene() != null &&
                            exportReservationsCsvButton.getScene().getWindow() != null) {
                        Stage stage = (Stage) exportReservationsCsvButton.getScene().getWindow();
                        viewModel.setStage(stage);
                        logger.info("Stage setat cu succes în ReportViewModel (a doua încercare): {}", stage);
                    } else {
                        logger.error("Nu s-a putut obține stage-ul pentru ReportViewModel - a doua încercare eșuată");
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Eroare la setarea stage-ului în ReportViewModel: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual method to set stage - can be called from MainViewController if needed
     */
    public void setStage(Stage stage) {
        if (stage != null && viewModel != null) {
            viewModel.setStage(stage);
            logger.info("Stage setat manual în ReportViewModel: {}", stage);
        }
    }
}