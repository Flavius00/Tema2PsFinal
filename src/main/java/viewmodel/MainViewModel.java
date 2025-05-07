package viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MainViewModel {
    private final ObjectProperty<Object> currentView = new SimpleObjectProperty<>();

    public MainViewModel() {
        // Initialize with a default view
        HotelViewModel hotelViewModel = new HotelViewModel();
        currentView.set(hotelViewModel);
    }

    public ObjectProperty<Object> currentViewProperty() {
        return currentView;
    }

    public void switchToHotelView() {
        HotelViewModel hotelViewModel = new HotelViewModel();
        currentView.set(hotelViewModel);
    }

    public void switchToRoomView() {
        RoomViewModel roomViewModel = new RoomViewModel();
        currentView.set(roomViewModel);
    }

    public void switchToReservationView() {
        ReservationViewModel reservationViewModel = new ReservationViewModel();
        currentView.set(reservationViewModel);
    }

    public void switchToReportView() {
        ReportViewModel reportViewModel = new ReportViewModel();
        currentView.set(reportViewModel);
    }
}