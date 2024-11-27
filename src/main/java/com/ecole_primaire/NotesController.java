package com.ecole_primaire;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

public class NotesController {

    @FXML
    private TableView<Note> noteTable;
    @FXML
    private TableColumn<Note, String> matiereColumn;
    @FXML
    private TableColumn<Note, Float> noteColumn;
    @FXML
    private Button backButton;

    private ObservableList<Note> notes = FXCollections.observableArrayList();
    static public String username;  // Store username (email)

    // Setter to pass the username
    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        // Initialize the table columns
        matiereColumn.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        if (username == null || username.isEmpty()) {
            System.out.println("Error: Current user not set!");
            return;
        }

        // Fetch student ID from the `etudiant` table based on current user (email)
        int etudiantId = getEtudiantIdFromDB(username);

        if (etudiantId == -1) {
            System.out.println("Error: Student ID not found for user: " + username);
            return;
        }

        // Load notes from DB based on the student ID
        loadNotesFromDB(etudiantId);
        noteTable.setItems(notes);
    }

    @FXML
    public void handleBackAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/dashboard_eleve.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getEtudiantIdFromDB(String username) {
        String query = "SELECT e.id_etudiant FROM etudiant e " +
                "JOIN users u ON u.username = e.email " +
                "WHERE u.username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_etudiant");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void loadNotesFromDB(int etudiantId) {
        String query = "SELECT m.nom_matiere, n.note " +
                "FROM note n JOIN matiere m ON n.matiere_id = m.matiere_id " +
                "WHERE n.id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(new Note(rs.getString("nom_matiere"), rs.getFloat("note")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
