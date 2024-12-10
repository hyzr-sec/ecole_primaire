package com.ecole_primaire;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class HomeController extends Application{
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    public void handleLogin(ActionEvent event) {
        System.out.println("Button clicked!");
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Get the user ID from the DAO
        int id = UserDao.getUser(username, password);

        if (id != -1) {
            System.out.println("Login successful!");
            String role = UserDao.getUserRole(id);
            String fxmlPath = "";

            // Select the correct FXML path based on the role
            switch (role) {
                case "directeur":
                    fxmlPath = "/layout/dashboard_directeur.fxml";
                    break;
                case "enseignant":
                    fxmlPath = "/layout/dashboard_enseignant.fxml";
                    break;
                case "eleve":
                    fxmlPath = "/layout/dashboard_eleve.fxml";
                    break;
                default:
                    System.out.println("Unknown role: " + role);
                    return;
            }

            try {
                // Load the corresponding FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent dashboardRoot = loader.load();

                // Pass data to the controller depending on the role
                switch (role) {
                    case "enseignant":
                        EnseignantController enseignantController = loader.getController();
                        enseignantController.setUsername(username);  // Pass username or more data if needed
                        break;

                    case "eleve":
                        EleveController eleveController = loader.getController();
                        eleveController.setUsername(username);  // Pass the username or other data
                        break;

                }

                // Show the dashboard
                Scene dashboardScene = new Scene(dashboardRoot);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(dashboardScene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error loading the dashboard view.");
            }

        } else {
            System.out.println("Login failed!");
        }
    }


    @FXML
    private void handleAbout(ActionEvent event) {
        System.out.println("About clicked!");
    }
    @FXML
    private void handleExit(ActionEvent event) {
        System.out.println("Exit clicked!");
        System.exit(0);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/layout/authentication.fxml"));
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setTitle("Ecole Primaire");
        primaryStage.getIcons().add(new Image(getClass().getResource("/assets/logo_school.png").toExternalForm()));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}