package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.Hotel;
import model.Reservation;
import model.Room;
import viewmodel.ReservationViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReservationViewController {
    private static final Logger logger = LoggerFactory.getLogger(ReservationViewController.class);

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
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button dateFilterButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button resetFiltersButton;

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
        logger.info("Inițializare ReservationViewController");

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

        // Setup action bindings pentru butoane
        saveButton.disableProperty().bind(viewModel.saveButtonDisabledProperty());
        saveButton.onActionProperty().bind(viewModel.saveActionProperty());

        deleteButton.disableProperty().bind(viewModel.selectedReservationProperty().isNull());
        deleteButton.onActionProperty().bind(viewModel.deleteActionProperty());

        clearButton.onActionProperty().bind(viewModel.clearActionProperty());

        dateFilterButton.disableProperty().bind(
                viewModel.selectedHotelProperty().isNull().or(viewModel.filterDateProperty().isNull())
        );
        dateFilterButton.onActionProperty().bind(viewModel.dateFilterActionProperty());

        searchButton.disableProperty().bind(viewModel.searchCustomerNameProperty().isEmpty());
        searchButton.onActionProperty().bind(viewModel.searchActionProperty());

        resetFiltersButton.onActionProperty().bind(viewModel.resetFiltersActionProperty());

        // Setup table columns
        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        roomInfoColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            if (reservation.getRoom() != null) {
                StringBuilder roomInfo = new StringBuilder(reservation.getRoom().getRoomNumber());
                if (reservation.getRoom().getRoomType() != null && !reservation.getRoom().getRoomType().isEmpty()) {
                    roomInfo.append(" - ").append(reservation.getRoom().getRoomType());
                }
                return new javafx.beans.property.SimpleStringProperty(roomInfo.toString());
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
            if (reservation.getStartDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        reservation.getStartDate().toLocalDate().format(formatter));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        checkOutColumn.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            if (reservation.getEndDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        reservation.getEndDate().toLocalDate().format(formatter));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        // Bind table items
        reservationTableView.setItems(viewModel.getReservations());
        logger.info("Număr rezervări în tabel: {}", viewModel.getReservations().size());

        // Setup table selection listener
        reservationTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewModel.selectedReservationProperty().set(newSelection);
            if (newSelection != null) {
                logger.info("Rezervare selectată: ID={}, Client={}", newSelection.getId(), newSelection.getCustomerName());
            }
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
                StringBuilder roomInfo = new StringBuilder(room.getRoomNumber());
                if (room.getRoomType() != null && !room.getRoomType().isEmpty()) {
                    roomInfo.append(" - ").append(room.getRoomType());
                }
                if (room.getPricePerNight() != null) {
                    roomInfo.append(" (").append(room.getPricePerNight()).append(" RON/noapte)");
                }
                return roomInfo.toString();
            }

            @Override
            public Room fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        // Setup payment status combo box
        paymentStatusComboBox.getItems().addAll("Pending", "Confirmed", "Paid", "Canceled");
        paymentStatusComboBox.valueProperty().bindBidirectional(viewModel.paymentStatusProperty());

        // Încarcă rezervările la inițializare
        viewModel.loadAllReservations();
    }
}