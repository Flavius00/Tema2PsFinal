module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires java.sql;
    requires org.slf4j;
    // requires org.apache.commons.csv; // Elimină această linie
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires commons.csv;

    // Deschide pachetele pentru reflecție (necesar pentru FXML)
    opens org.example to javafx.fxml;
    opens view to javafx.fxml;

    // Exportă pachetele pentru utilizare
    exports org.example;
    exports view;
}