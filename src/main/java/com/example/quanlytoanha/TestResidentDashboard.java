package com.example.quanlytoanha;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.service.AuthService;
import com.example.quanlytoanha.session.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestResidentDashboard extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Log in as John Doe
        AuthService authService = new AuthService();
        User user = authService.login("resident", "resident");

        if (user == null) {
            throw new RuntimeException("Failed to login as John Doe. Make sure the user exists in the database.");
        }

        // Load and show the resident dashboard
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/resident_dashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Resident Dashboard - " + user.getFullName());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
