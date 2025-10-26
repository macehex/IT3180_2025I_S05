package com.example.quanlytoanha;

import com.example.quanlytoanha.utils.DatabaseConnection;
import com.example.quanlytoanha.utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class AddJohnDoe {
    public static void main(String[] args) {
        // this is for one time testing purpose, please ignore this file

        // --- CẤU HÌNH USER CƯ DÂN ---
        int residentUserId = 3;
        String residentUsername = "resident";
        String residentPassword = "resident";
        String residentEmail = "johndoe_resident@example.com";
        String residentFullName = "John Doe";
        int residentRoleId = 4; // Role ID for RESIDENT

        try {
            String hashedPassword = PasswordUtil.hashPassword(residentPassword);
            System.out.println("Hashing password: " + residentPassword + " -> " + hashedPassword);

            String sql = "INSERT INTO users (user_id, username, password, email, full_name, role_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, residentUserId);
                pstmt.setString(2, residentUsername);
                pstmt.setString(3, hashedPassword);
                pstmt.setString(4, residentEmail);
                pstmt.setString(5, residentFullName);
                pstmt.setInt(6, residentRoleId);
                pstmt.setTimestamp(7, Timestamp.from(Instant.now()));

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("--- SUCCESS ---");
                    System.out.println("Added user '" + residentUsername + "' (ID=" + residentUserId + ") to the database.");
                    System.out.println("You can log in with password: '" + residentPassword + "'");
                } else {
                    System.out.println("--- FAILED ---");
                    System.out.println("Could not add user.");
                }
            }

        } catch (Exception e) {
            // This will likely fail if the user already exists, which is okay.
            System.err.println("Error adding resident user (they might already exist): " + e.getMessage());
        }
    }
}
