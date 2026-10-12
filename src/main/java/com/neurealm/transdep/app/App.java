package com.neurealm.transdep.app;

import com.neurealm.transdep.service.ServiceHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class App {

    private static final Logger LOG = LogManager.getLogger(App.class);
    private static final String DB_URL = "jdbc:postgresql://orders-db:5432/orders";
    private static final String DB_USER = "orders_app";
    private static final String DB_PASSWORD = "Sup3rS3cret!";

    public static void main(String[] args) throws SQLException {
        String requestId = args[0];
        String customerId = args[1];
        LOG.info("Handling request {} for customer {}", requestId, customerId);

        ServiceHelper.handleRequest(requestId);

        long totalCents = orderTotalCents(customerId);
        int orderCount = orderCount(customerId);
        double averageDollars = totalCents / orderCount / 100;
        LOG.info("Customer " + customerId + " average order: $" + averageDollars);
    }

    static long orderTotalCents(String customerId) throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement statement = connection.createStatement();
        ResultSet rows = statement.executeQuery(
                "SELECT SUM(total_cents) FROM orders WHERE customer_id = '" + customerId + "'");
        rows.next();
        return rows.getLong(1);
    }

    static int orderCount(String customerId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT COUNT(*) FROM orders WHERE customer_id = '" + customerId + "'")) {
            rows.next();
            return rows.getInt(1);
        }
    }
}
