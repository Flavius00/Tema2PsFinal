package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long roomId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Double totalPrice;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For joining with room information
    private Room room;

    public Reservation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Reservation(Long id, LocalDateTime startDate, LocalDateTime endDate, Long roomId,
                       String customerName, String customerEmail, String customerPhone) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roomId = roomId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.paymentStatus = "Pending";
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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

    // Calculate the number of nights
    public long getNumberOfNights() {
        return java.time.Duration.between(
                startDate.toLocalDate().atStartOfDay(),
                endDate.toLocalDate().atStartOfDay()).toDays();
    }

    // Calculate total price based on room price and number of nights
    public void calculateTotalPrice() {
        if (room != null && room.getPricePerNight() != null) {
            this.totalPrice = room.getPricePerNight() * getNumberOfNights();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation #" + id + " - " + customerName + " (" + startDate.toLocalDate() + " to " + endDate.toLocalDate() + ")";
    }
}