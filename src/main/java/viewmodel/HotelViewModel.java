package viewmodel;

import model.Chain;
import model.Hotel;
import model.Location;
import service.ChainService;
import service.HotelService;
import service.LocationService; // New import
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class HotelViewModel {
    private final HotelService hotelService;
    private final ChainService chainService;
    private final LocationService locationService; // New service

    private final ObservableList<Hotel> hotels = FXCollections.observableArrayList();
    private final ObjectProperty<Hotel> selectedHotel = new SimpleObjectProperty<>();

    private final LongProperty hotelId = new SimpleLongProperty(0);
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty phone = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty amenities = new SimpleStringProperty("");

    // Location as a selected object instead of individual fields
    private final ObjectProperty<Location> selectedLocation = new SimpleObjectProperty<>();
    private final ObservableList<Location> locations = FXCollections.observableArrayList();

    // Chain properties
    private final ObjectProperty<Chain> selectedChain = new SimpleObjectProperty<>();
    private final ObservableList<Chain> chains = FXCollections.observableArrayList();

    private final BooleanProperty saveButtonDisabled = new SimpleBooleanProperty(true);
    private final StringProperty statusMessage = new SimpleStringProperty("");

    public HotelViewModel() {
        this.hotelService = new HotelService();
        this.chainService = new ChainService();
        this.locationService = new LocationService(); // Initialize location service
        loadHotels();
        loadChains();
        loadLocations(); // Load locations from database

        // Bind the saveButton disabled state to name
        saveButtonDisabled.bind(name.isEmpty());

        // Add listener to selected hotel property
        selectedHotel.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                hotelId.set(newValue.getId());
                name.set(newValue.getName());
                phone.set(newValue.getPhone());
                email.set(newValue.getEmail());
                amenities.set(newValue.getAmenities());

                // Set location if available
                if (newValue.getLocation() != null) {
                    // Find matching location in our list
                    for (Location location : locations) {
                        if (location.getId().equals(newValue.getLocation().getId())) {
                            selectedLocation.set(location);
                            break;
                        }
                    }
                } else {
                    selectedLocation.set(null);
                }

                // Set chain if available
                if (newValue.getChainId() != null) {
                    for (Chain chain : chains) {
                        if (chain != null && chain.getId() != null &&
                                chain.getId().equals(newValue.getChainId())) {
                            selectedChain.set(chain);
                            break;
                        }
                    }
                } else {
                    selectedChain.set(null);
                }
            } else {
                clearForm();
            }
        });
    }

    public void loadHotels() {
        List<Hotel> hotelList = hotelService.getAllHotels();
        hotels.setAll(hotelList);
    }

    public void loadHotelsByChain(Chain chain) {
        if (chain != null && chain.getId() != null) {
            List<Hotel> hotelList = hotelService.getHotelsByChainId(chain.getId());
            hotels.setAll(hotelList);
        } else {
            loadHotels();
        }
    }

    public void loadChains() {
        List<Chain> chainList = chainService.getAllChains();
        chains.setAll(chainList);
    }

    // New method to load locations
    public void loadLocations() {
        List<Location> locationList = locationService.getAllLocations();
        locations.setAll(locationList);
    }

    public void saveHotel() {
        Hotel hotel = new Hotel();
        hotel.setName(name.get());
        hotel.setPhone(phone.get());
        hotel.setEmail(email.get());
        hotel.setAmenities(amenities.get());

        // Set chain ID if selected
        if (selectedChain.get() != null && selectedChain.get().getId() != null) {
            hotel.setChainId(selectedChain.get().getId());
        }

        // Set location if selected
        if (selectedLocation.get() != null) {
            hotel.setLocationId(selectedLocation.get().getId());
            hotel.setLocation(selectedLocation.get());
        }

        boolean success;
        if (hotelId.get() > 0) {
            hotel.setId(hotelId.get());
            success = hotelService.updateHotel(hotel);
            statusMessage.set(success ? "Hotel actualizat cu succes" : "Eroare la actualizarea hotelului");
        } else {
            success = hotelService.addHotel(hotel);
            statusMessage.set(success ? "Hotel adăugat cu succes" : "Eroare la adăugarea hotelului");
        }

        if (success) {
            loadHotels();
            clearForm();
        }
    }

    public void deleteHotel() {
        if (hotelId.get() > 0) {
            boolean success = hotelService.deleteHotel(hotelId.get());
            statusMessage.set(success ? "Hotel șters cu succes" : "Eroare la ștergerea hotelului");

            if (success) {
                loadHotels();
                clearForm();
            }
        }
    }

    public void clearForm() {
        hotelId.set(0);
        name.set("");
        phone.set("");
        email.set("");
        amenities.set("");
        selectedLocation.set(null);
        selectedChain.set(null);
        selectedHotel.set(null);
    }

    // Getters for observable properties
    public ObservableList<Hotel> getHotels() {
        return hotels;
    }

    public ObjectProperty<Hotel> selectedHotelProperty() {
        return selectedHotel;
    }

    public LongProperty hotelIdProperty() {
        return hotelId;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty amenitiesProperty() {
        return amenities;
    }

    // New property for location
    public ObjectProperty<Location> selectedLocationProperty() {
        return selectedLocation;
    }

    public ObservableList<Location> getLocations() {
        return locations;
    }

    public ObjectProperty<Chain> selectedChainProperty() {
        return selectedChain;
    }

    public ObservableList<Chain> getChains() {
        return chains;
    }

    public BooleanProperty saveButtonDisabledProperty() {
        return saveButtonDisabled;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
}