package com.ecole_primaire;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class HomeController extends Application{
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    private void handleLogin(ActionEvent event) {
        System.out.println("Button clicked!");
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (UserDao.getUser(username, password)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed!");
        }
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout clicked!");
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
        Parent root = FXMLLoader.load(getClass().getResource("/dashboard_enseignant/authentication.fxml"));
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