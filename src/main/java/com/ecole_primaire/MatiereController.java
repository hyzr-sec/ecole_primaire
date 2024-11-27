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

public class MatiereController {

    @FXML
    private TableView<Matiere> matiereTable;
    @FXML
    private TableColumn<Matiere, Integer> idColumn;
    @FXML
    private TableColumn<Matiere, String> nameColumn;
    @FXML
    private Button backButton;

    private ObservableList<Matiere> matieres = FXCollections.observableArrayList();
    public static String username;  // To store the username (if needed in this controller)

    // Setter to pass the username
    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        loadMatieresFromDB();
        matiereTable.setItems(matieres);
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

    private void loadMatieresFromDB() {
        String query = "SELECT matiere_id, nom_matiere FROM matiere";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                matieres.add(new Matiere(rs.getInt("matiere_id"), rs.getString("nom_matiere")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
