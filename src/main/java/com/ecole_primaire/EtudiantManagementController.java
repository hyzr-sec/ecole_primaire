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

public class EtudiantManagementController {
    @FXML
    private Button backToDashboardButton;
    @FXML
    private TableView<Etudiant> etudiantTable;
    @FXML
    private TableColumn<Etudiant, Integer> colId;
    @FXML
    private TableColumn<Etudiant, String> colNom;
    @FXML
    private TableColumn<Etudiant, Integer> colFiliere;
    @FXML
    private TableColumn<Etudiant, String> colEmail;
    @FXML
    private TableColumn<Etudiant, String> colDateNaissance;

    private ObservableList<Etudiant> etudiantList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdEtudiant()).asObject());
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomEtudiant()));
        colFiliere.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getFiliereId()).asObject());
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colDateNaissance.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateNaissance()));

        loadEtudiants();
    }

    private void loadEtudiants() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM etudiant";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            etudiantList.clear();
            while (rs.next()) {
                etudiantList.add(new Etudiant(
                        rs.getInt("id_etudiant"),
                        rs.getString("nom_etudiant"),
                        rs.getInt("filiere_id"),
                        rs.getString("email"),
                        rs.getString("date_naissance")
                ));
            }
            etudiantTable.setItems(etudiantList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddEtudiant() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Étudiant");
        dialog.setHeaderText("Enter Étudiant Details");
        dialog.setContentText("Format: name,filiereId,email,dateNaissance (yyyy-MM-dd)");

        // Show dialog and process result
        dialog.showAndWait().ifPresent(input -> {
            String[] fields = input.split(",");
            if (fields.length == 4) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Insert into the 'etudiant' table
                    String etudiantQuery = "INSERT INTO etudiant (nom_etudiant, filiere_id, email, date_naissance) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmtEtudiant = conn.prepareStatement(etudiantQuery);
                    stmtEtudiant.setString(1, fields[0]);
                    stmtEtudiant.setInt(2, Integer.parseInt(fields[1]));
                    stmtEtudiant.setString(3, fields[2]);
                    stmtEtudiant.setString(4, fields[3]);
                    stmtEtudiant.executeUpdate();

                    // Get the newly inserted 'etudiant' ID
                    String getLastIdQuery = "SELECT LAST_INSERT_ID()";
                    PreparedStatement stmtGetId = conn.prepareStatement(getLastIdQuery);
                    ResultSet rs = stmtGetId.executeQuery();
                    int newEtudiantId = 0;
                    if (rs.next()) {
                        newEtudiantId = rs.getInt(1);
                    }

                    // Insert into the 'users' table with role 'eleve'
                    String userQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                    PreparedStatement stmtUser = conn.prepareStatement(userQuery);
                    stmtUser.setString(1, fields[2]); // email as username
                    stmtUser.setString(2, fields[2]); // password (you can change this logic)
                    stmtUser.setString(3, "eleve"); // role
                    stmtUser.executeUpdate();

                    // Refresh the table
                    loadEtudiants();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Étudiant and User account added successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add étudiant and user: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid format. Please follow the specified format.");
            }
        });
    }

    @FXML
    private void handleEditEtudiant() {
        Etudiant selectedEtudiant = etudiantTable.getSelectionModel().getSelectedItem();
        if (selectedEtudiant == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an étudiant to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedEtudiant.getNomEtudiant() + "," +
                selectedEtudiant.getFiliereId() + "," +
                selectedEtudiant.getEmail() + "," +
                selectedEtudiant.getDateNaissance());
        dialog.setTitle("Edit Étudiant");
        dialog.setHeaderText("Update Étudiant Details");
        dialog.setContentText("Format: name,filiereId,email,dateNaissance (yyyy-MM-dd)");

        // Show dialog and process result
        dialog.showAndWait().ifPresent(input -> {
            String[] fields = input.split(",");
            if (fields.length == 4) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "UPDATE etudiant SET nom_etudiant = ?, filiere_id = ?, email = ?, date_naissance = ? WHERE id_etudiant = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, fields[0]);
                    stmt.setInt(2, Integer.parseInt(fields[1]));
                    stmt.setString(3, fields[2]);
                    stmt.setString(4, fields[3]);
                    stmt.setInt(5, selectedEtudiant.getIdEtudiant());
                    stmt.executeUpdate();

                    // Refresh table
                    loadEtudiants();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Étudiant updated successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update étudiant: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid format. Please follow the specified format.");
            }
        });
    }

    @FXML
    private void handleDeleteEtudiant() {
        Etudiant selectedEtudiant = etudiantTable.getSelectionModel().getSelectedItem();
        if (selectedEtudiant == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an étudiant to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Étudiant");
        confirmation.setHeaderText("Are you sure you want to delete this étudiant?");
        confirmation.setContentText("This action cannot be undone.");

        // Confirm deletion
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Delete from 'users' table
                    String deleteUserQuery = "DELETE FROM users WHERE username = ?";
                    PreparedStatement stmtDeleteUser = conn.prepareStatement(deleteUserQuery);
                    stmtDeleteUser.setString(1, selectedEtudiant.getEmail());
                    stmtDeleteUser.executeUpdate();

                    // Delete from 'etudiant' table
                    String deleteEtudiantQuery = "DELETE FROM etudiant WHERE id_etudiant = ?";
                    PreparedStatement stmtDeleteEtudiant = conn.prepareStatement(deleteEtudiantQuery);
                    stmtDeleteEtudiant.setInt(1, selectedEtudiant.getIdEtudiant());
                    stmtDeleteEtudiant.executeUpdate();

                    // Refresh table
                    loadEtudiants();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Étudiant and User account deleted successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete étudiant and user: " + e.getMessage());
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
