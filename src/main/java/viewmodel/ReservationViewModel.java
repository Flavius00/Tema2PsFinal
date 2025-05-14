package viewmodel;

import model.Hotel;
import model.Reservation;
import model.Room;
import service.HotelService;
import service.ReservationService;
import service.RoomService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationViewModel {
    private static final Logger logger = LoggerFactory.getLogger(ReservationViewModel.class);
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final HotelService hotelService;

    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private final ObjectProperty<Reservation> selectedReservation = new SimpleObjectProperty<>();

    private final ObservableList<Hotel> hotels = FXCollections.observableArrayList();
    private final ObjectProperty<Hotel> selectedHotel = new SimpleObjectProperty<>();

    private final ObservableList<Room> availableRooms = FXCollections.observableArrayList();
    private final ObjectProperty<Room> selectedRoom = new SimpleObjectProperty<>();

    private final LongProperty reservationId = new SimpleLongProperty(0);
    private final ObjectProperty<LocalDate> checkInDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> checkOutDate = new SimpleObjectProperty<>(LocalDate.now().plusDays(1));
    private final StringProperty customerName = new SimpleStringProperty("");
    private final StringProperty customerEmail = new SimpleStringProperty("");
    private final StringProperty customerPhone = new SimpleStringProperty("");
    private final DoubleProperty totalPrice = new SimpleDoubleProperty(0.0);
    private final StringProperty paymentStatus = new SimpleStringProperty("Pending");

    // Filter properties
    private final ObjectProperty<LocalDate> filterDate = new SimpleObjectProperty<>(LocalDate.now());
    private final StringProperty searchCustomerName = new SimpleStringProperty("");

    private final BooleanProperty saveButtonDisabled = new SimpleBooleanProperty(true);
    private final StringProperty statusMessage = new SimpleStringProperty("");

    public ReservationViewModel() {
        this.reservationService = new ReservationService();
        this.roomService = new RoomService();
        this.hotelService = new HotelService();

        // Încărcăm toate hotelurile
        loadHotels();

        // Încărcăm toate rezervările la început pentru a verifica funcționarea
        loadAllReservations();

        // Bind the saveButton disabled state
        saveButtonDisabled.bind(customerName.isEmpty()
                .or(selectedRoom.isNull())
                .or(checkInDate.isNull())
                .or(checkOutDate.isNull()));

        // Add listener to selected hotel property to load rooms for that hotel
        selectedHotel.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                logger.info("Hotel selectat: {} (ID: {})", newValue.getName(), newValue.getId());
                // Încărcăm toate rezervările pentru hotelul selectat (fără filtrare după dată)
                loadReservationsByHotelId(newValue.getId());
                updateAvailableRooms();
            } else {
                logger.info("Niciun hotel selectat, se afișează toate rezervările");
                loadAllReservations();
                availableRooms.clear();
            }
        });

        // Add listeners to check-in and check-out dates to update available rooms
        checkInDate.addListener((observable, oldValue, newValue) -> updateAvailableRooms());
        checkOutDate.addListener((observable, oldValue, newValue) -> updateAvailableRooms());

        // Add listener to selected room property to calculate price
        selectedRoom.addListener((observable, oldValue, newValue) -> calculateTotalPrice());

        // Add listener to selected reservation property
        selectedReservation.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                logger.info("Rezervare selectată: {}", newValue.getId());
                reservationId.set(newValue.getId());
                checkInDate.set(newValue.getStartDate().toLocalDate());
                checkOutDate.set(newValue.getEndDate().toLocalDate());
                customerName.set(newValue.getCustomerName());
                customerEmail.set(newValue.getCustomerEmail());
                customerPhone.set(newValue.getCustomerPhone());
                totalPrice.set(newValue.getTotalPrice() != null ? newValue.getTotalPrice() : 0.0);
                paymentStatus.set(newValue.getPaymentStatus());

                // Set room and hotel if not already set
                if (newValue.getRoom() != null) {
                    logger.info("Camera rezervării: {}", newValue.getRoom().getRoomNumber());
                    selectedRoom.set(newValue.getRoom());

                    if (newValue.getRoom().getHotel() != null) {
                        logger.info("Hotelul camerei: {}", newValue.getRoom().getHotel().getName());
                        selectedHotel.set(newValue.getRoom().getHotel());
                    } else if (selectedHotel.get() == null || !selectedHotel.get().getId().equals(newValue.getRoom().getHotelId())) {
                        for (Hotel hotel : hotels) {
                            if (hotel.getId().equals(newValue.getRoom().getHotelId())) {
                                selectedHotel.set(hotel);
                                break;
                            }
                        }
                    }
                }
            } else {
                clearReservationFields();
            }
        });
    }

    // Încărcăm toate rezervările
    public void loadAllReservations() {
        List<Reservation> reservationList = reservationService.getAllReservations();
        logger.info("Au fost încărcate {} rezervări", reservationList.size());
        reservations.setAll(reservationList);
    }

    // Încărcăm rezervările pentru un hotel specific (fără filtrare după dată)
    public void loadReservationsByHotelId(Long hotelId) {
        List<Reservation> reservationList = reservationService.getReservationsByHotelId(hotelId);
        logger.info("Au fost încărcate {} rezervări pentru hotelul cu ID-ul {}",
                reservationList.size(), hotelId);
        reservations.setAll(reservationList);
    }

    private void updateAvailableRooms() {
        if (selectedHotel.get() != null && checkInDate.get() != null && checkOutDate.get() != null) {
            // Convert LocalDate to LocalDateTime for start and end of day
            LocalDateTime startDateTime = checkInDate.get().atStartOfDay();
            LocalDateTime endDateTime = checkOutDate.get().atTime(LocalTime.MAX);

            List<Room> roomList = roomService.getAvailableRoomsByHotelIdAndDate(
                    selectedHotel.get().getId(), startDateTime, endDateTime);

            // If editing an existing reservation, include the currently selected room
            if (reservationId.get() > 0 && selectedRoom.get() != null) {
                boolean roomExists = false;
                for (Room room : roomList) {
                    if (room.getId().equals(selectedRoom.get().getId())) {
                        roomExists = true;
                        break;
                    }
                }
                if (!roomExists) {
                    roomList.add(selectedRoom.get());
                }
            }

            availableRooms.setAll(roomList);
            logger.info("Au fost găsite {} camere disponibile pentru intervalul {} - {}",
                    roomList.size(), startDateTime, endDateTime);
        } else {
            availableRooms.clear();
        }
    }

    private void calculateTotalPrice() {
        if (selectedRoom.get() != null && checkInDate.get() != null && checkOutDate.get() != null) {
            long days = java.time.Duration.between(
                    checkInDate.get().atStartOfDay(),
                    checkOutDate.get().atStartOfDay()).toDays();

            if (days <= 0) {
                days = 1; // Minimum 1 day
            }

            double price = selectedRoom.get().getPricePerNight() * days;
            totalPrice.set(price);
            logger.info("Preț total calculat: {} pentru {} zile", price, days);
        } else {
            totalPrice.set(0.0);
        }
    }
    public void loadHotels() {
        List<Hotel> hotelList = hotelService.getAllHotels();
        hotels.setAll(hotelList);
        logger.info("Au fost încărcate {} hoteluri", hotelList.size());
    }

    public void loadReservationsByHotelAndDate(Long hotelId, LocalDate date) {
        // Convert LocalDate to LocalDateTime at noon (middle of the day)
        LocalDateTime dateTime = date.atTime(12, 0);

        List<Reservation> reservationList = reservationService.getReservationsByHotelIdAndDate(hotelId, dateTime);
        logger.info("Au fost găsite {} rezervări pentru hotel {} la data {}",
                reservationList.size(), hotelId, date);
        reservations.setAll(reservationList);
    }

    public void loadReservationsByCustomerName(String name) {
        List<Reservation> reservationList = reservationService.getReservationsByCustomerName(name);
        logger.info("Au fost găsite {} rezervări pentru clientul {}", reservationList.size(), name);
        reservations.setAll(reservationList);
    }

    public void saveReservation() {
        if (selectedRoom.get() == null) {
            statusMessage.set("Selectați o cameră");
            return;
        }

        if (checkInDate.get() == null || checkOutDate.get() == null) {
            statusMessage.set("Selectați datele de check-in și check-out");
            return;
        }

        if (checkInDate.get().isAfter(checkOutDate.get())) {
            statusMessage.set("Data de check-in nu poate fi după data de check-out");
            return;
        }

        Reservation reservation = new Reservation();
        reservation.setStartDate(checkInDate.get().atStartOfDay());
        reservation.setEndDate(checkOutDate.get().atTime(LocalTime.MAX));
        reservation.setRoomId(selectedRoom.get().getId());
        reservation.setCustomerName(customerName.get());
        reservation.setCustomerEmail(customerEmail.get());
        reservation.setCustomerPhone(customerPhone.get());
        reservation.setTotalPrice(totalPrice.get());
        reservation.setPaymentStatus(paymentStatus.get());

        boolean success;
        if (reservationId.get() > 0) {
            reservation.setId(reservationId.get());
            success = reservationService.updateReservation(reservation);
            statusMessage.set(success ? "Rezervare actualizată cu succes" : "Eroare la actualizarea rezervării");
        } else {
            success = reservationService.addReservation(reservation);
            statusMessage.set(success ? "Rezervare adăugată cu succes" : "Eroare la adăugarea rezervării");
        }

        if (success) {
            if (selectedHotel.get() != null) {
                // Reîncărcăm rezervările pentru hotelul selectat
                loadReservationsByHotelId(selectedHotel.get().getId());
            } else {
                // Reîncărcăm toate rezervările
                loadAllReservations();
            }
            clearReservationFields();
        }
    }

    public void deleteReservation() {
        if (reservationId.get() > 0) {
            boolean success = reservationService.deleteReservation(reservationId.get());
            statusMessage.set(success ? "Rezervare ștearsă cu succes" : "Eroare la ștergerea rezervării");

            if (success) {
                if (selectedHotel.get() != null) {
                    // Reîncărcăm rezervările pentru hotelul selectat
                    loadReservationsByHotelId(selectedHotel.get().getId());
                } else {
                    // Reîncărcăm toate rezervările
                    loadAllReservations();
                }
                clearReservationFields();
            }
        }
    }

    public void filterReservationsByDate() {
        if (selectedHotel.get() != null && filterDate.get() != null) {
            loadReservationsByHotelAndDate(selectedHotel.get().getId(), filterDate.get());
        } else {
            // Dacă nu este selectat un hotel, încărcăm toate rezervările
            loadAllReservations();
        }
    }

    public void searchReservationsByCustomerName() {
        if (!searchCustomerName.get().isEmpty()) {
            loadReservationsByCustomerName(searchCustomerName.get());
        } else if (selectedHotel.get() != null) {
            loadReservationsByHotelId(selectedHotel.get().getId());
        } else {
            // Dacă nu este selectat un hotel și nu este specificat un nume, încărcăm toate rezervările
            loadAllReservations();
        }
    }

    public void clearReservationFields() {
        reservationId.set(0);
        checkInDate.set(LocalDate.now());
        checkOutDate.set(LocalDate.now().plusDays(1));
        customerName.set("");
        customerEmail.set("");
        customerPhone.set("");
        totalPrice.set(0.0);
        paymentStatus.set("Pending");
        selectedRoom.set(null);
        selectedReservation.set(null);
    }

    // Getters for observable properties
    public ObservableList<Reservation> getReservations() {
        return reservations;
    }

    public ObjectProperty<Reservation> selectedReservationProperty() {
        return selectedReservation;
    }

    public ObservableList<Hotel> getHotels() {
        return hotels;
    }

    public ObjectProperty<Hotel> selectedHotelProperty() {
        return selectedHotel;
    }

    public ObservableList<Room> getAvailableRooms() {
        return availableRooms;
    }

    public ObjectProperty<Room> selectedRoomProperty() {
        return selectedRoom;
    }

    public LongProperty reservationIdProperty() {
        return reservationId;
    }

    public ObjectProperty<LocalDate> checkInDateProperty() {
        return checkInDate;
    }

    public ObjectProperty<LocalDate> checkOutDateProperty() {
        return checkOutDate;
    }

    public StringProperty customerNameProperty() {
        return customerName;
    }

    public StringProperty customerEmailProperty() {
        return customerEmail;
    }

    public StringProperty customerPhoneProperty() {
        return customerPhone;
    }

    public DoubleProperty totalPriceProperty() {
        return totalPrice;
    }

    public StringProperty paymentStatusProperty() {
        return paymentStatus;
    }

    public ObjectProperty<LocalDate> filterDateProperty() {
        return filterDate;
    }

    public StringProperty searchCustomerNameProperty() {
        return searchCustomerName;
    }

    public BooleanProperty saveButtonDisabledProperty() {
        return saveButtonDisabled;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
}