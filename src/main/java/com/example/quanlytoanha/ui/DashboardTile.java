package com.example.quanlytoanha.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardTile extends VBox {
    private Label titleLabel;
    private Label valueLabel;
    private Label descriptionLabel;

    public DashboardTile(String title, String value, String description) {
        // Style the tile
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        setPadding(new Insets(15));
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setMinSize(200, 150);

        // Create and style the title
        titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #666;");

        // Create and style the value
        valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setStyle("-fx-text-fill: #2196F3;");

        // Create and style the description
        descriptionLabel = new Label(description);
        descriptionLabel.setFont(Font.font("System", 12));
        descriptionLabel.setStyle("-fx-text-fill: #999;");

        getChildren().addAll(titleLabel, valueLabel, descriptionLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setDescription(String description) {
        descriptionLabel.setText(description);
    }

    public String getValue() {
        return valueLabel.getText();
    }

    public String getDescription() {
        return descriptionLabel.getText();
    }
}
