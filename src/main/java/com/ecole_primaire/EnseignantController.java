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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EnseignantController {

    @FXML
    private Button notesButton;
    @FXML
    private Button matiereButton;
    @FXML
    private Button filiereButton;
    @FXML
    private Button profileButton;


    public static String username;

    // Setter to pass the username
    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        // Set Icons for Buttons
        notesButton.setGraphic(createImageView("/assets/notes_icon.png"));
        filiereButton.setGraphic(createImageView("/assets/profile_icon.png"));
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
        // Navigate to Profile View
        TeacherProfileController teacherProfileController = new TeacherProfileController();
        teacherProfileController.setUsername(username);  // Pass the username here
        loadView("/layout/TeacherProfileView.fxml", "Teacher Profile");
    }

    @FXML
    private void handleNotes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to get the subject based on the teacher's username (email)
            String query = "SELECT nom_matiere FROM matiere " +
                    "INNER JOIN enseignant ON matiere.enseignant_id = enseignant.enseignant_id " +
                    "WHERE enseignant.email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);  // Use the teacher's email (username)
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String subject = rs.getString("nom_matiere");  // Fetch the subject

                // Pass the fetched subject to the TeacherNotesController
                TeacherNotesController controller = new TeacherNotesController();
                controller.setSubject(subject);
                controller.setUsername(username);
            }
            // Load the FXML file for TeacherNotesView
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/TeacherNotesView.fxml"));
            Parent root = loader.load();  // This will load the FXML and return the root node

            // Change the scene
            Stage stage = (Stage) notesButton.getScene().getWindow();  // Use notesButton's scene to get the current stage
            Scene scene = new Scene(root);  // Create a new scene with the loaded root node
            stage.setScene(scene);  // Set the new scene for the stage
        } catch (Exception e) {
            e.printStackTrace();
        }
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
