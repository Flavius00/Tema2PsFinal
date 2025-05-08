package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Hotel;
import viewmodel.ReportViewModel;

public class ReportViewController {

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
    private Label statusLabel;

    @FXML
    private TextArea instructionsTextArea;

    @FXML
    private void initialize() {
        viewModel = new ReportViewModel();

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
    }

    @FXML
    private void handleExportReservationsCsvButton() {
        validateHotelSelection();
        viewModel.exportReservationsToCsv(getStage());
    }

    @FXML
    private void handleExportReservationsDocButton() {
        validateHotelSelection();
        viewModel.exportReservationsToDoc(getStage());
    }

    @FXML
    private void handleExportRoomsCsvButton() {
        validateHotelSelection();
        viewModel.exportAvailableRoomsToCsv(getStage());
    }

    @FXML
    private void handleExportRoomsDocButton() {
        validateHotelSelection();
        viewModel.exportAvailableRoomsToDoc(getStage());
    }

    private void validateHotelSelection() {
        if (viewModel.selectedHotelProperty().get() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenție");
            alert.setHeaderText("Hotel neselectat");
            alert.setContentText("Vă rugăm să selectați un hotel înainte de a genera raportul.");
            alert.showAndWait();
        }
    }

    private Stage getStage() {
        return (Stage) statusLabel.getScene().getWindow();
    }
}