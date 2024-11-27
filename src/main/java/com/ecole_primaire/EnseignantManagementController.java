package com.ecole_primaire;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnseignantManagementController {
    @FXML
    private Button backToDashboardButton;
    @FXML
    private TableView<Enseignant> enseignantTable;
    @FXML
    private TableColumn<Enseignant, Integer> colId;
    @FXML
    private TableColumn<Enseignant, String> colNom;
    @FXML
    private TableColumn<Enseignant, String> colEmail;
    @FXML
    private TableColumn<Enseignant, String> colMatiere;

    @FXML
    private ObservableList<Enseignant> enseignantList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdEnseignant()).asObject());
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomEnseignant()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colMatiere.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMatiere()));

        loadEnseignants();
    }

    private void loadEnseignants() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM enseignant";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            enseignantList.clear();
            while (rs.next()) {
                enseignantList.add(new Enseignant(
                        rs.getInt("enseignant_id"),
                        rs.getString("nom_enseignant"),
                        rs.getString("email"),
                        rs.getString("matiere") // Load "Matière" data
                ));
            }
            enseignantTable.setItems(enseignantList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddEnseignant() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Enseignant");
        dialog.setHeaderText("Enter Enseignant Details");
        dialog.setContentText("Format: name,email,matiere");

        dialog.showAndWait().ifPresent(input -> {
            String[] fields = input.split(",");
            if (fields.length == 3) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Insert into the 'enseignant' table
                    String enseignantQuery = "INSERT INTO enseignant (nom_enseignant, email, matiere) VALUES (?, ?, ?)";
                    PreparedStatement stmtEnseignant = conn.prepareStatement(enseignantQuery);
                    stmtEnseignant.setString(1, fields[0]);
                    stmtEnseignant.setString(2, fields[1]);
                    stmtEnseignant.setString(3, fields[2]);
                    stmtEnseignant.executeUpdate();

                    // Insert into the 'users' table with role 'enseignant'
                    String userQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                    PreparedStatement stmtUser = conn.prepareStatement(userQuery);
                    stmtUser.setString(1, fields[1]); // email as username
                    stmtUser.setString(2, fields[1]); // password (can be changed as needed)
                    stmtUser.setString(3, "enseignant"); // role
                    stmtUser.executeUpdate();

                    loadEnseignants();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Enseignant and user added successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add enseignant and user: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid format. Please follow the specified format.");
            }
        });
    }

    @FXML
    private void handleEditEnseignant() {
        Enseignant selectedEnseignant = enseignantTable.getSelectionModel().getSelectedItem();
        if (selectedEnseignant == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an enseignant to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(
                selectedEnseignant.getNomEnseignant() + "," + selectedEnseignant.getEmail() + "," + selectedEnseignant.getMatiere());
        dialog.setTitle("Edit Enseignant");
        dialog.setHeaderText("Update Enseignant Details");
        dialog.setContentText("Format: name,email,matiere");

        dialog.showAndWait().ifPresent(input -> {
            String[] fields = input.split(",");
            if (fields.length == 3) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Update the 'enseignant' table
                    String enseignantQuery = "UPDATE enseignant SET nom_enseignant = ?, email = ?, matiere = ? WHERE enseignant_id = ?";
                    PreparedStatement stmtEnseignant = conn.prepareStatement(enseignantQuery);
                    stmtEnseignant.setString(1, fields[0]);
                    stmtEnseignant.setString(2, fields[1]);
                    stmtEnseignant.setString(3, fields[2]);
                    stmtEnseignant.setInt(4, selectedEnseignant.getIdEnseignant());
                    stmtEnseignant.executeUpdate();

                    // Update the 'users' table for the same user
                    String userQuery = "UPDATE users SET username = ?, password = ? WHERE username = ?";
                    PreparedStatement stmtUser = conn.prepareStatement(userQuery);
                    stmtUser.setString(1, fields[1]); // email as username
                    stmtUser.setString(2, fields[1]); // password (can be changed as needed)
                    stmtUser.setString(3, selectedEnseignant.getEmail()); // existing email (username)
                    stmtUser.executeUpdate();

                    loadEnseignants();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Enseignant and user updated successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update enseignant and user: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid format. Please follow the specified format.");
            }
        });
    }

    @FXML
    private void handleDeleteEnseignant() {
        Enseignant selectedEnseignant = enseignantTable.getSelectionModel().getSelectedItem();
        if (selectedEnseignant == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an enseignant to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Enseignant");
        confirmation.setHeaderText("Are you sure you want to delete this enseignant?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Delete from the 'enseignant' table
                    String enseignantQuery = "DELETE FROM enseignant WHERE enseignant_id = ?";
                    PreparedStatement stmtEnseignant = conn.prepareStatement(enseignantQuery);
                    stmtEnseignant.setInt(1, selectedEnseignant.getIdEnseignant());
                    stmtEnseignant.executeUpdate();

                    // Delete from the 'users' table
                    String userQuery = "DELETE FROM users WHERE username = ?";
                    PreparedStatement stmtUser = conn.prepareStatement(userQuery);
                    stmtUser.setString(1, selectedEnseignant.getEmail()); // email is used as username
                    stmtUser.executeUpdate();

                    loadEnseignants();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Enseignant and user deleted successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete enseignant and user: " + e.getMessage());
                }
            }
        });
    }
    @FXML
    private void handleBackToDashboard() {
        // Switch scene to dashboard
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/dashboard_directeur.fxml"));
            Parent root = loader.load();
            Scene dashboardScene = new Scene(root);

            // Get the current stage and set the new scene
            Stage stage = (Stage) backToDashboardButton.getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
