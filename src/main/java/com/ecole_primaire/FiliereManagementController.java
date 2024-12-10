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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.mariadb.jdbc.Statement;

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
    private TableView<Matiere> matiereTable;
    @FXML
    private TableColumn<Matiere, Integer> colMatiereId;
    @FXML
    private TableColumn<Matiere, String> colMatiereName;
    @FXML
    private Button backToDashboardButton;
    @FXML
    private TableColumn<Matiere, Integer> colEnseignantId;

    private ObservableList<Filiere> filiereList = FXCollections.observableArrayList();
    private ObservableList<Matiere> matiereList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Initialize Filiere Table
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdFiliere()).asObject());
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomFiliere()));

        // Initialize Matiere Table
        colMatiereId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colMatiereName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colEnseignantId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getEnseignantId()).asObject());
        filiereTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            handleFiliereSelection(); // Manually call the handler when selection changes
        });
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

    private void loadMatieres(int filiereId) {
        System.out.println("3");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM matiere WHERE filiere_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, filiereId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("4");

            matiereList.clear();
            while (rs.next()) {
                int matiereId = rs.getInt("matiere_id");
                String nomMatiere = rs.getString("nom_matiere");
                Integer enseignantId = rs.getObject("enseignant_id", Integer.class);  // Handle potential NULL
                System.out.println(enseignantId);
                // Create a Matiere object with the 'enseignant_id' included
                Matiere matiere = new Matiere(matiereId, nomMatiere, enseignantId);
                matiereList.add(matiere);
            }

            matiereTable.setItems(matiereList);
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
    private void handleAddMatiere() {
        Filiere selectedFiliere = filiereTable.getSelectionModel().getSelectedItem();
        if (selectedFiliere == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a filiere to add a matiere.");
            return;
        }

        // Create a custom dialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Matiere");
        dialog.setHeaderText("Enter Matiere and Enseignant Information");

        // Set the button types.
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Create the labels and text fields for input
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField matiereNameField = new TextField();
        matiereNameField.setPromptText("Matiere Name");

        TextField enseignantNameField = new TextField();
        enseignantNameField.setPromptText("Enseignant Name");

        grid.add(new Label("Matiere Name:"), 0, 0);
        grid.add(matiereNameField, 1, 0);
        grid.add(new Label("Enseignant Name:"), 0, 1);
        grid.add(enseignantNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a Pair of Strings (matiereName, enseignantName)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Pair<>(matiereNameField.getText(), enseignantNameField.getText());
            }
            return null;
        });

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(result -> {
            String matiereName = result.getKey();
            String enseignantName = result.getValue();

            if (!matiereName.isEmpty() && !enseignantName.isEmpty()) {
                // Get enseignant_id from enseignantName
                Integer enseignantId = getSelectedEnseignantId(enseignantName);
                if (enseignantId == null) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "No valid enseignant selected.");
                    return;
                }

                // Insert the matiere with enseignant_id
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "INSERT INTO matiere (nom_matiere, filiere_id, enseignant_id) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, matiereName);
                    stmt.setInt(2, selectedFiliere.getIdFiliere());
                    stmt.setObject(3, enseignantId);  // Handle null if no enseignant is selected
                    stmt.executeUpdate();

                    // Get the generated matiere_id
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    int matiereId = -1;
                    if (generatedKeys.next()) {
                        matiereId = generatedKeys.getInt(1);
                    }

                    if (matiereId != -1) {
                        // Insert default grades for all students (note = 0)
                        String insertGradesQuery = "INSERT INTO note (id_etudiant, matiere_id, note) " +
                                "SELECT id_etudiant, ?, 0 FROM etudiant";
                        PreparedStatement gradeStmt = conn.prepareStatement(insertGradesQuery);
                        gradeStmt.setInt(1, matiereId);
                        gradeStmt.executeUpdate();

                        loadMatieres(selectedFiliere.getIdFiliere());
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Matiere added and students' grades initialized to 0.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add matiere: " + e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Both Matiere name and Enseignant name are required.");
            }
        });
    }

    private Integer getSelectedEnseignantId(String enseignantName) {
        // This method retrieves the enseignant_id based on the name
        String query = "SELECT enseignant_id FROM enseignant WHERE nom_enseignant = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, enseignantName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("enseignant_id");
            } else {
                System.out.println("No enseignant found with the name: " + enseignantName);
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void handleFiliereSelection() {
        Filiere selectedFiliere = filiereTable.getSelectionModel().getSelectedItem();
        System.out.println("1");
        if (selectedFiliere != null) {
            System.out.println("2");
            loadMatieres(selectedFiliere.getIdFiliere());
        } else {
            matiereList.clear();
        }
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/dashboard_directeur.fxml"));
            Parent root = loader.load();
            Scene dashboardScene = new Scene(root);

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
