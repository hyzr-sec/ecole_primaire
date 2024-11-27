package com.ecole_primaire;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.sql.*;

public class TeacherProfileController {

    @FXML
    private Label teacherIdLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label subjectLabel;
    @FXML
    private Button backButton;

    private Connection conn;
    public static String username; // Store username (email)

    // Setter to accept the username
    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() throws SQLException {
        conn = DatabaseConnection.getConnection();
        loadTeacherProfile();
    }

    private void loadTeacherProfile() {
        String query = "SELECT e.enseignant_id, e.nom_enseignant, e.email, e.matiere " +
                "FROM enseignant e " +
                "JOIN users u ON u.username = e.email " +
                "WHERE u.username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);  // Use the username (email) to fetch the data
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                teacherIdLabel.setText(String.valueOf(rs.getInt("enseignant_id")));
                nameLabel.setText(rs.getString("nom_enseignant"));
                emailLabel.setText(rs.getString("email"));
                subjectLabel.setText(rs.getString("matiere"));
            } else {
                showError("Error", "No teacher found with username " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error loading teacher profile.");
        }
    }

    // Handle the back button action
    @FXML
    private void handleBackAction() {
        try {
            // Load the Teacher Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/dashboard_enseignant.fxml"));
            Parent root = loader.load();

            // Get the current stage (window) and change the scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions (e.g., if FXML file is not found)
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
