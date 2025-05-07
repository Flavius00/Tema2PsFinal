module org.example.tema2ps {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.slf4j;
    requires org.apache.commons.csv;
    requires org.apache.poi.ooxml;

    opens org.example to javafx.fxml;
    exports org.example;
}