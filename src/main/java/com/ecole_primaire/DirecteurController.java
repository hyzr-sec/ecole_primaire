package com.ecole_primaire;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import java.io.IOException;

public class DirecteurController {

    @FXML
    private Button eleveButton;
    @FXML
    private Button enseignantButton;
    @FXML
    private Button filiereButton;
    @FXML
    private Button moyennesButton;


    @FXML
    public void initialize() {
        // Set Icons and Tooltips for Buttons
        setButtonProperties(eleveButton, "/assets/eleve_icon.png", "Manage Élèves");
        setButtonProperties(enseignantButton, "/assets/enseignant_icon.png", "Manage Enseignants");
        setButtonProperties(filiereButton, "/assets/filiere_icon.png", "Manage Filières");
        setButtonProperties(moyennesButton, "/assets/moyenne.png", "Moyennes élèves");
    }

    private void setButtonProperties(Button button, String iconPath, String tooltipText) {
        button.setGraphic(createImageView(iconPath));
        button.setTooltip(new Tooltip(tooltipText));
    }

    private ImageView createImageView(String imageUrl) {
        ImageView imageView = new ImageView(new Image(getClass().getResource(imageUrl).toExternalForm()));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void navigateToView(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) eleveButton.getScene().getWindow(); // Reuse current stage
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root, 600, 600);
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            showAlert("Error", "Could not load the view: " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    private void handleMoyennes(ActionEvent event) {
        navigateToView("/layout/StudentAveragesView.fxml", "Moyennes des Étudiants");
    }


    @FXML
    private void handleEleve(ActionEvent event) {
        navigateToView("/layout/EleveManagementView.fxml", "Gestion des Élèves");
    }

    @FXML
    private void handleEnseignant(ActionEvent event) {
        navigateToView("/layout/EnseignantManagementView.fxml", "Gestion des Enseignants");
    }

    @FXML
    private void handleFiliere(ActionEvent event) {
        navigateToView("/layout/FiliereManagementView.fxml", "Gestion des Filières");
    }

    @FXML
    private void handleLogout() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");
        Stage currentStage = (Stage) eleveButton.getScene().getWindow();
        alert.initOwner(currentStage);

        alert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("OK")) {
                navigateToView("/layout/authentication.fxml", "Login");
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
