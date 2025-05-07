package service;

import model.Hotel;
import model.Room;
import repository.HotelRepository;
import repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService() {
        this.roomRepository = new RoomRepository();
        this.hotelRepository = new HotelRepository();
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    public List<Room> getRoomsByHotelIdAndPriceRange(Long hotelId, Double minPrice, Double maxPrice) {
        return roomRepository.findByHotelIdAndPrice(hotelId, minPrice, maxPrice);
    }

    public List<Room> getAvailableRoomsByHotelIdAndDate(Long hotelId, LocalDateTime startDate, LocalDateTime endDate) {
        return roomRepository.findAvailableRoomsByHotelIdAndDate(hotelId, startDate, endDate);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public boolean addRoom(Room room) {
        try {
            // Verify the hotel exists
            if (room.getHotelId() != null) {
                Optional<Hotel> hotel = hotelRepository.findById(room.getHotelId());
                if (!hotel.isPresent()) {
                    logger.error("Cannot add room: Hotel with id {} not found", room.getHotelId());
                    return false;
                }
            }

            Long id = roomRepository.save(room);
            if (id != null) {
                room.setId(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error adding room: {} in hotel id: {}", room.getRoomNumber(), room.getHotelId(), e);
            return false;
        }
    }

    public boolean updateRoom(Room room) {
        try {
            // Verify the hotel exists
            if (room.getHotelId() != null) {
                Optional<Hotel> hotel = hotelRepository.findById(room.getHotelId());
                if (!hotel.isPresent()) {
                    logger.error("Cannot update room: Hotel with id {} not found", room.getHotelId());
                    return false;
                }
            }

            return roomRepository.update(room);
        } catch (Exception e) {
            logger.error("Error updating room with id: {}", room.getId(), e);
            return false;
        }
    }

    public boolean deleteRoom(Long id) {
        try {
            return roomRepository.delete(id);
        } catch (Exception e) {
            logger.error("Error deleting room with id: {}", id, e);
            return false;
        }
    }

    public List<String> getAllRoomTypes() {
        return roomRepository.findAllRoomTypes();
    }
}