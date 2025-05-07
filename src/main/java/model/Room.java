package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room {
    private Long id;
    private Long hotelId;
    private String roomNumber;
    private Double pricePerNight;
    private Long imageId;
    private String amenities;
    private String roomType;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For joining with hotel information
    private Hotel hotel;

    // List of reservations for this room
    private List<Reservation> reservations;

    public Room() {
        this.reservations = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Room(Long id, Long hotelId, String roomNumber, Double pricePerNight, String amenities, String roomType, Integer capacity) {
        this.id = id;
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.pricePerNight = pricePerNight;
        this.amenities = amenities;
        this.roomType = roomType;
        this.capacity = capacity;
        this.reservations = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(Double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " - " + roomType;
    }

    // Helper method to check if room is available for a given date range
    public boolean isAvailable(LocalDateTime startDate, LocalDateTime endDate) {
        for (Reservation reservation : reservations) {
            // If there's any overlap with existing reservations, the room is not available
            if (!(endDate.isBefore(reservation.getStartDate()) || startDate.isAfter(reservation.getEndDate()))) {
                return false;
            }
        }
        return true;
    }
}