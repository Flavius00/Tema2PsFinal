module org.example.tema2ps {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires java.sql;
    requires org.slf4j;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires commons.csv;

    // Open packages for reflection (needed for FXML)
    opens org.example.tema2ps to javafx.fxml;
    opens view to javafx.fxml;
    opens model to javafx.base;
    opens viewmodel to javafx.base;

    // Export packages for use
    exports org.example.tema2ps;
    exports view;
    exports model;
    exports service;
    exports repository;
    exports util;
    exports viewmodel;
}