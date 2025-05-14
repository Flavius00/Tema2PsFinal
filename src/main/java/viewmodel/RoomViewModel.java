package viewmodel;

import model.Hotel;
import model.Room;
import service.HotelService;
import service.RoomService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class RoomViewModel {
    private final RoomService roomService;
    private final HotelService hotelService;

    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObjectProperty<Room> selectedRoom = new SimpleObjectProperty<>();

    private final ObservableList<Hotel> hotels = FXCollections.observableArrayList();
    private final ObjectProperty<Hotel> selectedHotel = new SimpleObjectProperty<>();

    private final LongProperty roomId = new SimpleLongProperty(0);
    private final StringProperty roomNumber = new SimpleStringProperty("");
    private final DoubleProperty pricePerNight = new SimpleDoubleProperty(0.0);
    private final StringProperty amenities = new SimpleStringProperty("");
    private final StringProperty roomType = new SimpleStringProperty("");
    private final IntegerProperty capacity = new SimpleIntegerProperty(2); // Valoare implicită: 2

    // Filter properties
    private final DoubleProperty minPrice = new SimpleDoubleProperty(0.0);
    private final DoubleProperty maxPrice = new SimpleDoubleProperty(1000.0);
    private final ObjectProperty<LocalDate> availabilityDate = new SimpleObjectProperty<>(LocalDate.now());

    private final ObservableList<String> roomTypes = FXCollections.observableArrayList();

    private final BooleanProperty saveButtonDisabled = new SimpleBooleanProperty(true);
    private final StringProperty statusMessage = new SimpleStringProperty("");

    public RoomViewModel() {
        this.roomService = new RoomService();
        this.hotelService = new HotelService();
        loadHotels();
        loadRoomTypes();

        // Bind the saveButton disabled state
        saveButtonDisabled.bind(roomNumber.isEmpty().or(selectedHotel.isNull()));

        // Add listener to selected hotel property to load rooms for that hotel
        selectedHotel.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadRoomsByHotel(newValue.getId());
            } else {
                rooms.clear();
            }
        });

        // Add listener to selected room property
        selectedRoom.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                roomId.set(newValue.getId());
                roomNumber.set(newValue.getRoomNumber());
                pricePerNight.set(newValue.getPricePerNight());
                amenities.set(newValue.getAmenities());
                roomType.set(newValue.getRoomType());

                // Verificăm dacă capacitatea este setată corect
                if (newValue.getCapacity() != null) {
                    capacity.set(newValue.getCapacity());
                    System.out.println("Camera selectată are capacitatea: " + newValue.getCapacity());
                } else {
                    capacity.set(2); // Valoare implicită dacă nu există
                    System.out.println("Camera selectată nu are capacitate setată, folosim valoarea implicită: 2");
                }

                // Set hotel if not already set
                if (selectedHotel.get() == null || !selectedHotel.get().getId().equals(newValue.getHotelId())) {
                    for (Hotel hotel : hotels) {
                        if (hotel.getId().equals(newValue.getHotelId())) {
                            selectedHotel.set(hotel);
                            break;
                        }
                    }
                }
            } else {
                clearRoomFields();
            }
        });
    }

    public void loadHotels() {
        List<Hotel> hotelList = hotelService.getAllHotels();
        hotels.setAll(hotelList);
    }

    public void loadRoomsByHotel(Long hotelId) {
        List<Room> roomList = roomService.getRoomsByHotelId(hotelId);
        rooms.setAll(roomList);
    }

    public void loadRoomsByHotelAndPriceRange(Long hotelId, Double minPrice, Double maxPrice) {
        List<Room> roomList = roomService.getRoomsByHotelIdAndPriceRange(hotelId, minPrice, maxPrice);
        rooms.setAll(roomList);
    }

    public void loadAvailableRoomsByHotelAndDate(Long hotelId, LocalDate date) {
        // Convert LocalDate to LocalDateTime for start and end of day
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

        List<Room> roomList = roomService.getAvailableRoomsByHotelIdAndDate(hotelId, startDateTime, endDateTime);
        rooms.setAll(roomList);
    }

    public void loadRoomTypes() {
        List<String> types = roomService.getAllRoomTypes();
        roomTypes.setAll(types);

        // Add common room types if the list is empty
        if (types.isEmpty()) {
            roomTypes.addAll("Single", "Double", "Twin", "Suite", "Deluxe", "Family", "Presidential");
        }
    }

    public void saveRoom() {
        if (selectedHotel.get() == null) {
            statusMessage.set("Selectați un hotel");
            return;
        }

        Room room = new Room();
        room.setRoomNumber(roomNumber.get());
        room.setHotelId(selectedHotel.get().getId());
        room.setPricePerNight(pricePerNight.get());
        room.setAmenities(amenities.get());
        room.setRoomType(roomType.get());
        room.setCapacity(capacity.get());

        System.out.println("Salvăm camera cu capacitatea: " + capacity.get());

        boolean success;
        if (roomId.get() > 0) {
            room.setId(roomId.get());
            success = roomService.updateRoom(room);
            statusMessage.set(success ? "Cameră actualizată cu succes" : "Eroare la actualizarea camerei");
        } else {
            success = roomService.addRoom(room);
            statusMessage.set(success ? "Cameră adăugată cu succes" : "Eroare la adăugarea camerei");
        }

        if (success && selectedHotel.get() != null) {
            loadRoomsByHotel(selectedHotel.get().getId());
            clearRoomFields();
        }
    }

    public void deleteRoom() {
        if (roomId.get() > 0) {
            boolean success = roomService.deleteRoom(roomId.get());
            statusMessage.set(success ? "Cameră ștearsă cu succes" : "Eroare la ștergerea camerei");

            if (success && selectedHotel.get() != null) {
                loadRoomsByHotel(selectedHotel.get().getId());
                clearRoomFields();
            }
        }
    }

    public void filterRoomsByPrice() {
        if (selectedHotel.get() != null) {
            loadRoomsByHotelAndPriceRange(selectedHotel.get().getId(), minPrice.get(), maxPrice.get());
        }
    }

    public void filterRoomsByAvailability() {
        if (selectedHotel.get() != null && availabilityDate.get() != null) {
            loadAvailableRoomsByHotelAndDate(selectedHotel.get().getId(), availabilityDate.get());
        }
    }

    public void clearRoomFields() {
        roomId.set(0);
        roomNumber.set("");
        pricePerNight.set(0.0);
        amenities.set("");
        roomType.set("");
        capacity.set(2); // Resetăm la valoarea implicită
        selectedRoom.set(null);
    }

    public void clearFilters() {
        minPrice.set(0.0);
        maxPrice.set(1000.0);
        availabilityDate.set(LocalDate.now());

        if (selectedHotel.get() != null) {
            loadRoomsByHotel(selectedHotel.get().getId());
        }
    }

    // Getters for observable properties
    public ObservableList<Room> getRooms() {
        return rooms;
    }

    public ObjectProperty<Room> selectedRoomProperty() {
        return selectedRoom;
    }

    public ObservableList<Hotel> getHotels() {
        return hotels;
    }

    public ObjectProperty<Hotel> selectedHotelProperty() {
        return selectedHotel;
    }

    public LongProperty roomIdProperty() {
        return roomId;
    }

    public StringProperty roomNumberProperty() {
        return roomNumber;
    }

    public DoubleProperty pricePerNightProperty() {
        return pricePerNight;
    }

    public StringProperty amenitiesProperty() {
        return amenities;
    }

    public StringProperty roomTypeProperty() {
        return roomType;
    }

    public IntegerProperty capacityProperty() {
        return capacity;
    }

    public DoubleProperty minPriceProperty() {
        return minPrice;
    }

    public DoubleProperty maxPriceProperty() {
        return maxPrice;
    }

    public ObjectProperty<LocalDate> availabilityDateProperty() {
        return availabilityDate;
    }

    public ObservableList<String> getRoomTypes() {
        return roomTypes;
    }

    public BooleanProperty saveButtonDisabledProperty() {
        return saveButtonDisabled;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
}