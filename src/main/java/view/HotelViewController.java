package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.Chain;
import model.Hotel;
import model.Location;
import service.LocationService;
import viewmodel.HotelViewModel;

import java.util.List;

public class HotelViewController {

    private HotelViewModel viewModel;
    private LocationService locationService;

    @FXML
    private ComboBox<Chain> chainComboBox;

    @FXML
    private ComboBox<Chain> filterChainComboBox;

    @FXML
    private ComboBox<Location> locationComboBox;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField phoneTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextArea amenitiesTextArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button filterButton;

    @FXML
    private Button resetButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<Hotel> hotelTableView;

    @FXML
    private TableColumn<Hotel, Long> idColumn;

    @FXML
    private TableColumn<Hotel, String> nameColumn;

    @FXML
    private TableColumn<Hotel, String> locationColumn;

    @FXML
    private TableColumn<Hotel, String> phoneColumn;

    @FXML
    private TableColumn<Hotel, String> emailColumn;

    @FXML
    private TableColumn<Hotel, String> chainColumn;

    @FXML
    private void initialize() {
        viewModel = new HotelViewModel();
        locationService = new LocationService();

        // Setup bindings for form fields
        nameTextField.textProperty().bindBidirectional(viewModel.nameProperty());
        phoneTextField.textProperty().bindBidirectional(viewModel.phoneProperty());
        emailTextField.textProperty().bindBidirectional(viewModel.emailProperty());
        amenitiesTextArea.textProperty().bindBidirectional(viewModel.amenitiesProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // Setup location ComboBox
        loadLocations();
        locationComboBox.setConverter(new StringConverter<Location>() {
            @Override
            public String toString(Location location) {
                return location == null ? "" : location.getCity() + ", " + location.getCountry();
            }

            @Override
            public Location fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        // Add listener to location ComboBox
        locationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.locationIdProperty().set(newVal.getId());
                viewModel.countryProperty().set(newVal.getCountry());
                viewModel.cityProperty().set(newVal.getCity());
                viewModel.streetProperty().set(newVal.getStreet());
                viewModel.numberProperty().set(newVal.getNumber());
            } else {
                viewModel.locationIdProperty().set(0);
                viewModel.countryProperty().set("");
                viewModel.cityProperty().set("");
                viewModel.streetProperty().set("");
                viewModel.numberProperty().set("");
            }
        });

        // Add listener to selected hotel property to update location ComboBox
        viewModel.selectedHotelProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getLocation() != null) {
                // Find matching location in ComboBox
                for (Location location : locationComboBox.getItems()) {
                    if (location.getId().equals(newVal.getLocation().getId())) {
                        locationComboBox.setValue(location);
                        break;
                    }
                }
            } else {
                locationComboBox.setValue(null);
            }
        });

        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(cellData -> {
            Hotel hotel = cellData.getValue();
            if (hotel.getLocation() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        hotel.getLocation().getCity() + ", " + hotel.getLocation().getCountry());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        chainColumn.setCellValueFactory(cellData -> {
            Hotel hotel = cellData.getValue();
            if (hotel.getChain() != null) {
                return new javafx.beans.property.SimpleStringProperty(hotel.getChain().getName());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Bind table items
        hotelTableView.setItems(viewModel.getHotels());

        // Setup table selection listener
        hotelTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewModel.selectedHotelProperty().set(newSelection);
        });

        // Setup chain combo boxes
        chainComboBox.setItems(viewModel.getChains());
        chainComboBox.valueProperty().bindBidirectional(viewModel.selectedChainProperty());
        chainComboBox.setConverter(new StringConverter<Chain>() {
            @Override
            public String toString(Chain chain) {
                return chain == null ? "" : chain.getName();
            }

            @Override
            public Chain fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        filterChainComboBox.setItems(viewModel.getChains());
        filterChainComboBox.getItems().add(0, null); // Add null option for "All chains"
        filterChainComboBox.setConverter(new StringConverter<Chain>() {
            @Override
            public String toString(Chain chain) {
                return chain == null ? "Toate lanțurile" : chain.getName();
            }

            @Override
            public Chain fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        // Setup action bindings pentru butoane
        saveButton.disableProperty().bind(viewModel.saveButtonDisabledProperty());
        saveButton.onActionProperty().bind(viewModel.saveActionProperty());

        deleteButton.disableProperty().bind(viewModel.selectedHotelProperty().isNull());
        deleteButton.onActionProperty().bind(viewModel.deleteActionProperty());

        clearButton.onActionProperty().bind(viewModel.clearActionProperty());

        filterButton.onActionProperty().bind(viewModel.filterActionProperty());

        resetButton.onActionProperty().bind(viewModel.resetActionProperty());
    }

    private void loadLocations() {
        List<Location> locations = locationService.getAllLocations();
        locationComboBox.getItems().clear();
        locationComboBox.getItems().addAll(locations);
    }
}