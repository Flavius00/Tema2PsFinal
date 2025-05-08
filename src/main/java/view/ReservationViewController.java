package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.Hotel;
import model.Reservation;
import model.Room;
import viewmodel.ReservationViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReservationViewController {

    private ReservationViewModel viewModel;

    @FXML
    private ComboBox<Hotel> hotelComboBox;

    @FXML
    private ComboBox<Room> roomComboBox;

    @FXML
    private DatePicker checkInDatePicker;

    @FXML
    private DatePicker checkOutDatePicker;

    @FXML
    private TextField customerNameTextField;

    @FXML
    private TextField customerEmailTextField;

    @FXML
    private TextField customerPhoneTextField;

    @FXML
    private TextField totalPriceTextField;

    @FXML
    private ComboBox<String> paymentStatusComboBox;

    @FXML
    private DatePicker filterDatePicker;

    @FXML
    private TextField searchCustomerTextField;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<Reservation> reservationTableView;

    @FXML
    private TableColumn<Reservation, Long> reservationIdColumn;

    @FXML
    private TableColumn<Reservation, String> customerNameColumn;

    @FXML
    private TableColumn<Reservation, String> roomInfoColumn;

    @FXML
    private TableColumn<Reservation, String> hotelNameColumn;

    @FXML
    private TableColumn<Reservation, String> checkInColumn;

    @FXML
    private TableColumn<Reservation, String> checkOutColumn;

    @FXML
    private TableColumn<Reservation, Double> totalPriceColumn;

    @FXML
    private TableColumn<Reservation, String> paymentStatusColumn;

    @FXML
    private void initialize() {
        viewModel = new ReservationViewModel();

        // Setup bindings for form fields
        customerNameTextField.textProperty().bindBidirectional(viewModel.customerNameProperty());
        customerEmailTextField.textProperty().bindBidirectional(viewModel.customerEmailProperty());
        customerPhoneTextField.textProperty().bindBidirectional(viewModel.customerPhoneProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // Setup date pickers
        checkInDatePicker.valueProperty().bindBidirectional(viewModel.checkInDateProperty());
        checkOutDatePicker.valueProperty().bindBidirectional(viewModel.checkOutDateProperty());
        filterDatePicker.valueProperty().bindBidirectional(viewModel.filterDateProperty());

        // Setup total price field (read-only)
        totalPriceTextField.textProperty().bind(viewModel.totalPriceProperty().asString("%.2f"));

        // Setup search field
        searchCustomerTextField.textProperty().bindBidirectional(viewModel.searchCustomerNameProperty());

        // Setup table columns
        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        roomInfoColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            if (reservation.getRoom() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        reservation.getRoom().getRoomNumber() + " - " + reservation.getRoom().getRoomType());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        hotelNameColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            if (reservation.getRoom() != null && reservation.getRoom().getHotel() != null) {
                return new javafx.beans.property.SimpleStringProperty(reservation.getRoom().getHotel().getName());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Format dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        checkInColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    reservation.getStartDate().toLocalDate().format(formatter));
        });
        checkOutColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    reservation.getEndDate().toLocalDate().format(formatter));
        });

        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        // Bind table items
        reservationTableView.setItems(viewModel.getReservations());

        // Setup table selection listener
        reservationTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewModel.selectedReservationProperty().set(newSelection);
        });

        // Setup hotel combo box
        hotelComboBox.setItems(viewModel.getHotels());
        hotelComboBox.valueProperty().bindBidirectional(viewModel.selectedHotelProperty());
        hotelComboBox.setConverter(new StringConverter<Hotel>() {
            @Override
            public String toString(Hotel hotel) {
                return hotel == null ? "" : hotel.getName();
            }

            @Override
            public Hotel fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        // Setup room combo box
        roomComboBox.setItems(viewModel.getAvailableRooms());
        roomComboBox.valueProperty().bindBidirectional(viewModel.selectedRoomProperty());
        roomComboBox.setConverter(new StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                if (room == null) return "";
                return room.getRoomNumber() + " - " + room.getRoomType() +
                        " (" + room.getPricePerNight() + " RON/noapte)";
            }

            @Override
            public Room fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        // Setup payment status combo box
        paymentStatusComboBox.getItems().addAll("Pending", "Confirmed", "Paid", "Canceled");
        paymentStatusComboBox.valueProperty().bindBidirectional(viewModel.paymentStatusProperty());
    }

    @FXML
    private void handleSaveButton() {
        viewModel.saveReservation();
    }

    @FXML
    private void handleDeleteButton() {
        if (reservationTableView.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmare ștergere");
            alert.setHeaderText("Șterge rezervare");
            alert.setContentText("Sigur doriți să ștergeți rezervarea selectată?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    viewModel.deleteReservation();
                }
            });
        } else {
            statusLabel.setText("Selectați o rezervare pentru a o șterge");
        }
    }

    @FXML
    private void handleClearButton() {
        viewModel.clearReservationFields();
        reservationTableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDateFilterButton() {
        viewModel.filterReservationsByDate();
    }

    @FXML
    private void handleSearchButton() {
        viewModel.searchReservationsByCustomerName();
    }

    @FXML
    private void handleResetFiltersButton() {
        searchCustomerTextField.clear();
        if (hotelComboBox.getValue() != null) {
            viewModel.filterReservationsByDate();
        }
    }
}