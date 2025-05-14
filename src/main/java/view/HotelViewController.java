package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.Chain;
import model.Hotel;
import model.Location;
import viewmodel.HotelViewModel;

public class HotelViewController {

    private HotelViewModel viewModel;

    @FXML
    private ComboBox<Chain> chainComboBox;

    @FXML
    private ComboBox<Chain> filterChainComboBox;

    @FXML
    private ComboBox<Location> locationComboBox; // New ComboBox for locations

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField phoneTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextArea amenitiesTextArea;

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

        // Setup bindings for form fields
        nameTextField.textProperty().bindBidirectional(viewModel.nameProperty());
        phoneTextField.textProperty().bindBidirectional(viewModel.phoneProperty());
        emailTextField.textProperty().bindBidirectional(viewModel.emailProperty());
        amenitiesTextArea.textProperty().bindBidirectional(viewModel.amenitiesProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

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

        // Setup location combo box
        locationComboBox.setItems(viewModel.getLocations());
        locationComboBox.valueProperty().bindBidirectional(viewModel.selectedLocationProperty());
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
    }

    @FXML
    private void handleSaveButton() {
        viewModel.saveHotel();
    }

    @FXML
    private void handleDeleteButton() {
        if (hotelTableView.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmare ștergere");
            alert.setHeaderText("Șterge hotel");
            alert.setContentText("Sigur doriți să ștergeți hotelul selectat?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    viewModel.deleteHotel();
                }
            });
        } else {
            statusLabel.setText("Selectați un hotel pentru a-l șterge");
        }
    }

    @FXML
    private void handleClearButton() {
        viewModel.clearForm();
        hotelTableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleFilterButton() {
        Chain selectedChain = filterChainComboBox.getValue();
        viewModel.loadHotelsByChain(selectedChain);
    }

    @FXML
    private void handleResetButton() {
        filterChainComboBox.setValue(null);
        viewModel.loadHotels();
    }
}