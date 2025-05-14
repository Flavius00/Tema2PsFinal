package viewmodel;

import model.Chain;
import model.Hotel;
import model.Location;
import service.ChainService;
import service.HotelService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.List;

public class HotelViewModel {
    private final HotelService hotelService;
    private final ChainService chainService;

    private final ObservableList<Hotel> hotels = FXCollections.observableArrayList();
    private final ObjectProperty<Hotel> selectedHotel = new SimpleObjectProperty<>();

    private final LongProperty hotelId = new SimpleLongProperty(0);
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty phone = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty amenities = new SimpleStringProperty("");

    // Location properties - păstrăm aceste proprietăți pentru a fi utilizate de serviciu,
    // dar UI-ul va utiliza un ComboBox pentru a selecta locația
    private final LongProperty locationId = new SimpleLongProperty(0);
    private final StringProperty country = new SimpleStringProperty("");
    private final StringProperty city = new SimpleStringProperty("");
    private final StringProperty street = new SimpleStringProperty("");
    private final StringProperty number = new SimpleStringProperty("");

    // Chain properties
    private final ObjectProperty<Chain> selectedChain = new SimpleObjectProperty<>();
    private final ObservableList<Chain> chains = FXCollections.observableArrayList();

    private final BooleanProperty saveButtonDisabled = new SimpleBooleanProperty(true);
    private final StringProperty statusMessage = new SimpleStringProperty("");

    // Action properties pentru butoane
    private final ObjectProperty<EventHandler<ActionEvent>> saveAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> deleteAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> clearAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> filterAction = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<ActionEvent>> resetAction = new SimpleObjectProperty<>();

    public HotelViewModel() {
        this.hotelService = new HotelService();
        this.chainService = new ChainService();
        loadHotels();
        loadChains();

        // Bind the saveButton disabled state to name
        saveButtonDisabled.bind(name.isEmpty());

        // Setăm acțiunile pentru butoane
        saveAction.set(event -> saveHotel());
        deleteAction.set(event -> deleteHotel());
        clearAction.set(event -> clearForm());
        filterAction.set(event -> {
            if (selectedChain.get() != null) {
                loadHotelsByChain(selectedChain.get());
            }
        });
        resetAction.set(event -> {
            selectedChain.set(null);
            loadHotels();
        });

        // Add listener to selected hotel property
        selectedHotel.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                hotelId.set(newValue.getId());
                name.set(newValue.getName());
                phone.set(newValue.getPhone());
                email.set(newValue.getEmail());
                amenities.set(newValue.getAmenities());

                // Set location properties if available
                if (newValue.getLocation() != null) {
                    Location location = newValue.getLocation();
                    locationId.set(location.getId());
                    country.set(location.getCountry());
                    city.set(location.getCity());
                    street.set(location.getStreet());
                    number.set(location.getNumber());
                } else {
                    clearLocationFields();
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

        // Create location object
        if (locationId.get() > 0 || !city.get().isEmpty() || !country.get().isEmpty() || !street.get().isEmpty()) {
            Location location = new Location();
            location.setId(locationId.get() > 0 ? locationId.get() : null);
            location.setCountry(country.get());
            location.setCity(city.get());
            location.setStreet(street.get());
            location.setNumber(number.get());
            hotel.setLocation(location);
            hotel.setLocationId(location.getId());
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
        clearLocationFields();
        selectedChain.set(null);
        selectedHotel.set(null);
    }

    private void clearLocationFields() {
        locationId.set(0);
        country.set("");
        city.set("");
        street.set("");
        number.set("");
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

    public LongProperty locationIdProperty() {
        return locationId;
    }

    public StringProperty countryProperty() {
        return country;
    }

    public StringProperty cityProperty() {
        return city;
    }

    public StringProperty streetProperty() {
        return street;
    }

    public StringProperty numberProperty() {
        return number;
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

    // Getters for action properties
    public ObjectProperty<EventHandler<ActionEvent>> saveActionProperty() {
        return saveAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> deleteActionProperty() {
        return deleteAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> clearActionProperty() {
        return clearAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> filterActionProperty() {
        return filterAction;
    }

    public ObjectProperty<EventHandler<ActionEvent>> resetActionProperty() {
        return resetAction;
    }
}