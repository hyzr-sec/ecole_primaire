package com.ecole_primaire;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class EleveController {

    @FXML
    private Button notesButton;
    @FXML
    private Button matiereButton;
    @FXML
    private Button filiereButton;
    @FXML
    private Button moyennesButton;
    public static String username;

    // Setter to pass the username
    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        // Set Icons for Buttons
        notesButton.setGraphic(createImageView("/assets/notes_icon.png"));
        matiereButton.setGraphic(createImageView("/assets/matiere_icon.png"));
        filiereButton.setGraphic(createImageView("/assets/filiere_icon.png"));
    }

    private ImageView createImageView(String imageUrl) {
        ImageView imageView = new ImageView(new Image(getClass().getResource(imageUrl).toExternalForm()));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void loadView(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) notesButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        System.out.println("Navigating to Profile section.");

        // Pass the username to the ProfileController (set via constructor or setter)
        ProfileController profileController = new ProfileController();
        profileController.setUsername(username);  // Pass the username here

        loadView("/layout/ProfileView.fxml", "Liste des Matières");
    }

    @FXML
    private void handleMatiere(ActionEvent event) {
        System.out.println("Navigating to Matière section.");

        // Create an instance of MatiereController and set the username
        MatiereController matiereController = new MatiereController();
        matiereController.setUsername(username);  // Pass the username here

        loadView("/layout/MatiereView.fxml", "Liste des Matières");
    }

    @FXML
    private void handleNotes(ActionEvent event) {
        System.out.println("Navigating to Notes section.");

        // Create an instance of NotesController and set the username
        NotesController notesController = new NotesController();
        notesController.setUsername(username);  // Pass the username here

        loadView("/layout/NotesView.fxml", "Notes de l'Étudiant");
    }
    @FXML
    private void handleMoyennes(ActionEvent event) {
        loadView("/layout/StudentAveragesView.fxml", "Moyennes des Étudiants");
    }
    @FXML
    private void handleLogout() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");

        // Get the current stage from the notesButton scene (or any other button's scene)
        Stage currentStage = (Stage) notesButton.getScene().getWindow();
        alert.initOwner(currentStage);  // Set the current stage as the alert's owner

        alert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("OK")) {
                Parent root = null;
                try {
                    // Load the authentication FXML
                    root = FXMLLoader.load(getClass().getResource("/layout/authentication.fxml"));
                } catch (IOException e) {
                    e.printStackTrace();  // Log the error for debugging
                    throw new RuntimeException("Error loading authentication screen", e);
                }

                // Create a new scene with the loaded FXML
                Scene scene = new Scene(root, 600, 600);

                // Reuse the current stage and set the new scene
                currentStage.setScene(scene);
                currentStage.setTitle("Ecole Primaire");
                currentStage.getIcons().add(new Image(getClass().getResource("/assets/logo_school.png").toExternalForm()));
                currentStage.show();  // Make sure to show the stage after setting the scene
            }
        });
    }
}
