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

public class ProfileController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label filiereLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label dobLabel;
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
        loadStudentProfile();
    }

    public void handleBackAction() {
        try {
            // Load the Eleve Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/dashboard_eleve.fxml"));
            Parent root = loader.load();

            // Get the current stage (window) and change the scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions (e.g., if FXML file is not found)
        }
    }

    private void loadStudentProfile() {
        String query = "SELECT e.nom_etudiant, e.filiere_id, e.email, e.date_naissance " +
                "FROM etudiant e " +
                "JOIN users u ON u.username = e.email " +
                "WHERE u.username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);  // Use the username (email) to fetch the data
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameLabel.setText(rs.getString("nom_etudiant"));
                filiereLabel.setText(getFiliereName(rs.getInt("filiere_id")));
                emailLabel.setText(rs.getString("email"));
                dobLabel.setText(rs.getString("date_naissance"));
            } else {
                showError("Error", "No student found with username " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error loading student profile.");
        }
    }

    private String getFiliereName(int filiereId) {
        // Assuming you have a method to fetch the filiere name by ID from the database
        String query = "SELECT nom_filiere FROM filiere WHERE filiere_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, filiereId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom_filiere");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleEditProfile() {
        // Implement editing functionality (optional)
        System.out.println("Editing the student profile...");
    }
}
