package com.ecole_primaire;

import com.ecole_primaire.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;

public class TeacherNotesController {
    @FXML
    private TableView<StudentGrade> notesTable;
    @FXML
    private TableColumn<StudentGrade, String> studentNameColumn;
    @FXML
    private TableColumn<StudentGrade, String> gradeColumn;
    @FXML
    private TableColumn<StudentGrade, Void> editColumn;
    @FXML
    private Button saveButton; // Save changes button
    @FXML
    private Button backButton;

    private ObservableList<StudentGrade> studentGrades = FXCollections.observableArrayList();

    private Connection conn;
    public static String subject;
    public static String username; // Store username (email)

    // Setter methods for subject and username
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() throws Exception{
        conn = DatabaseConnection.getConnection(); // Establish connection
        studentNameColumn.setCellValueFactory(cellData -> cellData.getValue().studentNameProperty());
        gradeColumn.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());

        // Edit button logic
        editColumn.setCellFactory(column -> new TableCell<StudentGrade, Void>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setOnAction(event -> handleEdit(getTableRow().getItem()));
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
            }
        });

        loadStudentGrades();  // Load the data after initialization
    }

    private void loadStudentGrades() {
        // Ensure the connection is open
        try {
            if (conn == null || conn.isClosed()) {
                try {
                    conn = DatabaseConnection.getConnection();  // Reconnect if closed
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Connection Error", "Unable to re-establish the connection.");
                    return;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Query to get the teacher's matiere_id
        String matiereQuery = "SELECT matiere_id FROM matiere " +
                "JOIN enseignant e ON e.enseignant_id = matiere.enseignant_id " +
                "WHERE e.email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(matiereQuery)) {
            stmt.setString(1, username); // Using the teacher's username (email) to find their matiere
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int matiereId = rs.getInt("matiere_id");
                System.out.println(matiereId);

                // Query to get students studying the same matiere
                String studentQuery = "SELECT s.nom_etudiant, n.note FROM note n " +
                        "JOIN etudiant s ON s.id_etudiant = n.id_etudiant " +
                        "WHERE n.matiere_id = ?";
                try (PreparedStatement studentStmt = conn.prepareStatement(studentQuery)) {
                    studentStmt.setInt(1, matiereId);
                    ResultSet studentRs = studentStmt.executeQuery();

                    studentGrades.clear();
                    while (studentRs.next()) {
                        String studentName = studentRs.getString("nom_etudiant");
                        String grade = studentRs.getString("note");
                        studentGrades.add(new StudentGrade(studentName, grade));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Database Error", "Error fetching students and grades.");
                }
            } else {
                showError("Error", "No subject found for this teacher.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching matiere information.");
        }

        notesTable.setItems(studentGrades);  // Set the student grades to the table
    }

    private void handleEdit(StudentGrade studentGrade) {
        // Handle edit logic for grade
        System.out.println("Editing grade for: " + studentGrade.getStudentName());
        TextInputDialog dialog = new TextInputDialog(studentGrade.getGrade());
        dialog.setTitle("Edit Grade");
        dialog.setHeaderText("Enter new grade");
        dialog.setContentText("Grade:");
        dialog.showAndWait().ifPresent(newGrade -> studentGrade.setGrade(newGrade));
    }

    @FXML
    private void handleSave() {
        try {
            if (conn == null || conn.isClosed()) {
                try {
                    conn = DatabaseConnection.getConnection();  // Reconnect if closed
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Connection Error", "Unable to re-establish the connection.");
                    return;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Start a transaction for updating grades
        try {
            conn.setAutoCommit(false); // Disable auto-commit

            for (StudentGrade studentGrade : studentGrades) {
                String updateGradeQuery = "UPDATE note SET note = ? WHERE id_etudiant = (SELECT id_etudiant FROM etudiant WHERE nom_etudiant = ?) AND matiere_id = (SELECT matiere_id FROM matiere WHERE nom_matiere = ?)";
                try (PreparedStatement stmt = conn.prepareStatement(updateGradeQuery)) {
                    stmt.setString(1, studentGrade.getGrade());
                    stmt.setString(2, studentGrade.getStudentName());
                    stmt.setString(3, subject); // Assuming subject is the teacher's subject
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    conn.rollback();  // Rollback if error occurs during any update
                    e.printStackTrace();
                    showError("Database Error", "Error updating grades.");
                    return;
                }
            }

            conn.commit();  // Commit if all updates succeed
            showSuccess("Grades updated successfully!");
        } catch (SQLException e) {
            try {
                conn.rollback();  // Rollback the transaction if any error occurs
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            showError("Database Error", "Transaction failed.");
        } finally {
            try {
                conn.setAutoCommit(true);  // Restore auto-commit after the transaction
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackAction() {
        try {
            // Load the Teacher Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/dashboard_enseignant.fxml"));
            Parent root = loader.load();

            // Get the current stage (window) and change the scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions (e.g., if FXML file is not found)
        }
    }

    // StudentGrade class to represent the data in the table
    public static class StudentGrade {
        private final StringProperty studentName;
        private final StringProperty grade;

        public StudentGrade(String studentName, String grade) {
            this.studentName = new SimpleStringProperty(studentName);
            this.grade = new SimpleStringProperty(grade);
        }

        public String getStudentName() {
            return studentName.get();
        }

        public StringProperty studentNameProperty() {
            return studentName;
        }

        public String getGrade() {
            return grade.get();
        }

        public StringProperty gradeProperty() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade.set(grade);
        }
    }
}
