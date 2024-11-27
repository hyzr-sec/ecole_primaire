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

public class FiliereManagementController {

    @FXML
    private TableView<Filiere> filiereTable;
    @FXML
    private TableColumn<Filiere, Integer> colId;
    @FXML
    private TableColumn<Filiere, String> colName;
    @FXML
    private Button backToDashboardButton;
    @FXML
    private ObservableList<Filiere> filiereList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdFiliere()).asObject());
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomFiliere()));

        loadFilieres();
    }

    private void loadFilieres() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM filiere";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            filiereList.clear();
            while (rs.next()) {
                filiereList.add(new Filiere(
                        rs.getInt("filiere_id"),
                        rs.getString("nom_filiere")
                ));
            }
            filiereTable.setItems(filiereList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddFiliere() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Filiere");
        dialog.setHeaderText("Enter Filiere Name");
        dialog.setContentText("Filiere Name:");

        dialog.showAndWait().ifPresent(filiereName -> {
            if (!filiereName.isEmpty()) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Insert into the 'filiere' table
                    String query = "INSERT INTO filiere (nom_filiere) VALUES (?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, filiereName);
                    stmt.executeUpdate();

                    loadFilieres();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Filiere added successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add filiere: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Filiere name cannot be empty.");
            }
        });
    }

    @FXML
    private void handleEditFiliere() {
        Filiere selectedFiliere = filiereTable.getSelectionModel().getSelectedItem();
        if (selectedFiliere == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a filiere to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedFiliere.getNomFiliere());
        dialog.setTitle("Edit Filiere");
        dialog.setHeaderText("Update Filiere Name");
        dialog.setContentText("Filiere Name:");

        dialog.showAndWait().ifPresent(filiereName -> {
            if (!filiereName.isEmpty()) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Update the 'filiere' table
                    String query = "UPDATE filiere SET nom_filiere = ? WHERE filiere_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, filiereName);
                    stmt.setInt(2, selectedFiliere.getIdFiliere());
                    stmt.executeUpdate();

                    loadFilieres();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Filiere updated successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update filiere: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Filiere name cannot be empty.");
            }
        });
    }

    @FXML
    private void handleDeleteFiliere() {
        Filiere selectedFiliere = filiereTable.getSelectionModel().getSelectedItem();
        if (selectedFiliere == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a filiere to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Filiere");
        confirmation.setHeaderText("Are you sure you want to delete this filiere?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Delete from the 'filiere' table
                    String query = "DELETE FROM filiere WHERE filiere_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, selectedFiliere.getIdFiliere());
                    stmt.executeUpdate();

                    loadFilieres();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Filiere deleted successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete filiere: " + e.getMessage());
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
