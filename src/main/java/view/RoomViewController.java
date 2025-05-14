package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.Hotel;
import model.Room;
import viewmodel.RoomViewModel;

import java.time.LocalDate;

public class RoomViewController {

    private RoomViewModel viewModel;

    @FXML
    private ComboBox<Hotel> hotelComboBox;

    @FXML
    private TextField roomNumberTextField;

    @FXML
    private ComboBox<String> roomTypeComboBox;

    @FXML
    private Spinner<Integer> capacitySpinner;

    @FXML
    private TextField priceTextField;

    @FXML
    private TextArea amenitiesTextArea;

    @FXML
    private TextField minPriceTextField;

    @FXML
    private TextField maxPriceTextField;

    @FXML
    private DatePicker availabilityDatePicker;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<Room> roomTableView;

    @FXML
    private TableColumn<Room, Long> roomIdColumn;

    @FXML
    private TableColumn<Room, String> roomNumberColumn;

    @FXML
    private TableColumn<Room, String> roomTypeColumn;

    @FXML
    private TableColumn<Room, Integer> capacityColumn;

    @FXML
    private TableColumn<Room, Double> priceColumn;

    @FXML
    private TableColumn<Room, String> roomAmenitiesColumn;

    @FXML
    private void initialize() {
        viewModel = new RoomViewModel();

        // Setup bindings for form fields
        roomNumberTextField.textProperty().bindBidirectional(viewModel.roomNumberProperty());
        priceTextField.textProperty().bindBidirectional(viewModel.pricePerNightProperty(),
                new javafx.util.StringConverter<Number>() {
                    @Override
                    public String toString(Number object) {
                        return object == null ? "" : object.toString();
                    }

                    @Override
                    public Number fromString(String string) {
                        if (string == null || string.isEmpty()) {
                            return 0.0;
                        }
                        try {
                            return Double.parseDouble(string);
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }
                });
        amenitiesTextArea.textProperty().bindBidirectional(viewModel.amenitiesProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // Setup spinner cu valori între 1 și 10
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2);
        capacitySpinner.setValueFactory(valueFactory);

        // Creăm bind-ul bidirecțional între spinner și proprietatea capacity
        viewModel.capacityProperty().addListener((obs, oldVal, newVal) -> {
            // Actualizăm valoarea din spinner când se schimbă proprietatea capacityProperty
            capacitySpinner.getValueFactory().setValue(newVal.intValue());
        });

        // Și invers, actualizăm proprietatea când se schimbă spinner-ul
        capacitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.capacityProperty().set(newVal);
        });

        // Setup filter fields
        minPriceTextField.textProperty().bindBidirectional(viewModel.minPriceProperty(),
                new javafx.util.StringConverter<Number>() {
                    @Override
                    public String toString(Number object) {
                        return object == null ? "" : object.toString();
                    }

                    @Override
                    public Number fromString(String string) {
                        if (string == null || string.isEmpty()) {
                            return 0.0;
                        }
                        try {
                            return Double.parseDouble(string);
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }
                });
        maxPriceTextField.textProperty().bindBidirectional(viewModel.maxPriceProperty(),
                new javafx.util.StringConverter<Number>() {
                    @Override
                    public String toString(Number object) {
                        return object == null ? "" : object.toString();
                    }

                    @Override
                    public Number fromString(String string) {
                        if (string == null || string.isEmpty()) {
                            return 1000.0;
                        }
                        try {
                            return Double.parseDouble(string);
                        } catch (NumberFormatException e) {
                            return 1000.0;
                        }
                    }
                });
        availabilityDatePicker.valueProperty().bindBidirectional(viewModel.availabilityDateProperty());

        // Setup table columns
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        roomAmenitiesColumn.setCellValueFactory(new PropertyValueFactory<>("amenities"));

        // Bind table items
        roomTableView.setItems(viewModel.getRooms());

        // Setup table selection listener
        roomTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewModel.selectedRoomProperty().set(newSelection);
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

        // Setup room type combo box
        roomTypeComboBox.setItems(viewModel.getRoomTypes());
        roomTypeComboBox.valueProperty().bindBidirectional(viewModel.roomTypeProperty());
    }

    @FXML
    private void handleSaveButton() {
        // Asigurăm că valoarea curentă din spinner este setată în model înainte de salvare
        viewModel.capacityProperty().set(capacitySpinner.getValue());
        viewModel.saveRoom();
    }

    @FXML
    private void handleDeleteButton() {
        if (roomTableView.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmare ștergere");
            alert.setHeaderText("Șterge cameră");
            alert.setContentText("Sigur doriți să ștergeți camera selectată?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    viewModel.deleteRoom();
                }
            });
        } else {
            statusLabel.setText("Selectați o cameră pentru a o șterge");
        }
    }

    @FXML
    private void handleClearButton() {
        viewModel.clearRoomFields();
        roomTableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handlePriceFilterButton() {
        viewModel.filterRoomsByPrice();
    }

    @FXML
    private void handleAvailabilityFilterButton() {
        viewModel.filterRoomsByAvailability();
    }

    @FXML
    private void handleResetFiltersButton() {
        viewModel.clearFilters();
    }
}