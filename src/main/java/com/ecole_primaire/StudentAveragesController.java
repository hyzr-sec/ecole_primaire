package com.ecole_primaire;

import com.ecole_primaire.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentAveragesController {

    @FXML
    private TableView<StudentAverage> tableView;

    @FXML
    private TableColumn<StudentAverage, String> colStudentName;

    @FXML
    private TableColumn<StudentAverage, Double> colAverage;
    @FXML
    private Button backButton;

    private ObservableList<StudentAverage> studentAverages = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colAverage.setCellValueFactory(new PropertyValueFactory<>("average"));

        loadStudentAverages();
        tableView.setItems(studentAverages);
    }
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/layout/dashboard_directeur.fxml")); // Path to the Directeur Dashboard
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Directeur Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadStudentAverages() {
        String query = """
            SELECT e.nom_etudiant, AVG(n.note) AS moyenne
            FROM etudiant e
            INNER JOIN note n ON e.id_etudiant = n.id_etudiant
            GROUP BY e.nom_etudiant
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String studentName = rs.getString("nom_etudiant");
                double average = rs.getDouble("moyenne");
                studentAverages.add(new StudentAverage(studentName, average));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error (show alert, log, etc.)
        }
    }
}
