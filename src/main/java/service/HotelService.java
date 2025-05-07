package service;

import model.Hotel;
import model.Location;
import model.Room;
import repository.HotelRepository;
import repository.LocationRepository;
import repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class HotelService {
    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);
    private final HotelRepository hotelRepository;
    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;

    public HotelService() {
        this.hotelRepository = new HotelRepository();
        this.locationRepository = new LocationRepository();
        this.roomRepository = new RoomRepository();
    }

    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();
        for (Hotel hotel : hotels) {
            loadAdditionalData(hotel);
        }
        return hotels;
    }

    public List<Hotel> getHotelsByChainId(Long chainId) {
        List<Hotel> hotels = hotelRepository.findByChainId(chainId);
        for (Hotel hotel : hotels) {
            loadAdditionalData(hotel);
        }
        return hotels;
    }

    public Optional<Hotel> getHotelById(Long id) {
        Optional<Hotel> hotelOpt = hotelRepository.findById(id);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            loadAdditionalData(hotel);
            return Optional.of(hotel);
        }
        return Optional.empty();
    }

    private void loadAdditionalData(Hotel hotel) {
        // Load location if needed
        if (hotel.getLocation() == null && hotel.getLocationId() != null) {
            Optional<Location> location = locationRepository.findById(hotel.getLocationId());
            location.ifPresent(hotel::setLocation);
        }

        // Load rooms
        List<Room> rooms = roomRepository.findByHotelId(hotel.getId());
        hotel.setRooms(rooms);
    }

    public boolean addHotel(Hotel hotel) {
        try {
            // First, save or update location if provided
            if (hotel.getLocation() != null) {
                Location location = hotel.getLocation();
                if (location.getId() == null || location.getId() <= 0) {
                    // New location, save it
                    Long locationId = locationRepository.save(location);
                    if (locationId != null) {
                        location.setId(locationId);
                        hotel.setLocationId(locationId);
                    }
                } else {
                    // Existing location, update it
                    locationRepository.update(location);
                    hotel.setLocationId(location.getId());
                }
            }

            // Save the hotel
            Long id = hotelRepository.save(hotel);
            if (id != null) {
                hotel.setId(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error adding hotel: " + hotel.getName(), e);
            return false;
        }
    }

    public boolean updateHotel(Hotel hotel) {
        try {
            // First, update location if provided
            if (hotel.getLocation() != null) {
                Location location = hotel.getLocation();
                if (location.getId() == null || location.getId() <= 0) {
                    // New location, save it
                    Long locationId = locationRepository.save(location);
                    if (locationId != null) {
                        location.setId(locationId);
                        hotel.setLocationId(locationId);
                    }
                } else {
                    // Existing location, update it
                    locationRepository.update(location);
                    hotel.setLocationId(location.getId());
                }
            }

            // Update the hotel
            return hotelRepository.update(hotel);
        } catch (Exception e) {
            logger.error("Error updating hotel: " + hotel.getName(), e);
            return false;
        }
    }

    public boolean deleteHotel(Long id) {
        try {
            return hotelRepository.delete(id);
        } catch (Exception e) {
            logger.error("Error deleting hotel with id: " + id, e);
            return false;
        }
    }
}