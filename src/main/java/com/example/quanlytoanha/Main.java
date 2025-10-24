// Vị trí: src/main/java/com/example/quanlytoanha/Main.java
package com.example.quanlytoanha;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }
}