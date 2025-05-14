package org.example.tema2ps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_chain";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.debug("Conexiune reușită la baza de date");
            return conn;
        } catch (SQLException e) {
            logger.error("Eroare la conectarea la baza de date: {}", e.getMessage());
            throw e;
        }
    }
}