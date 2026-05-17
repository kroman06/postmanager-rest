package net.kozachok.postmanager;

import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDatabaseInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext context) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres")) {
            conn.createStatement().execute("CREATE DATABASE postmanager_test");
        } catch (SQLException e) {
            if (!"42P04".equals(e.getSQLState())) { // not duplicate_database
                throw new RuntimeException("Failed to create test database", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}