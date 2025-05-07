package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainViewController {

    @FXML
    private Label titleLabel;

    @FXML
    private Button hotelButton;

    @FXML
    private Button roomButton;

    @FXML
    private Button reservationButton;

    @FXML
    private Button reportButton;

    @FXML
    private StackPane contentPane;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        // Inițializare cu view-ul de hoteluri la pornire
        switchToHotelView();
    }

    @FXML
    private void switchToHotelView() {
        loadView("/fxml/hotel.fxml");
        statusLabel.setText("Gestionare hoteluri");
    }

    @FXML
    private void switchToRoomView() {
        loadView("/fxml/room.fxml");
        statusLabel.setText("Gestionare camere");
    }

    @FXML
    private void switchToReservationView() {
        loadView("/fxml/reservation.fxml");
        statusLabel.setText("Gestionare rezervări");
    }

    @FXML
    private void switchToReportView() {
        loadView("/fxml/report.fxml");
        statusLabel.setText("Generare rapoarte");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            statusLabel.setText("Eroare la încărcarea interfeței: " + e.getMessage());
            e.printStackTrace();
        }
    }
}