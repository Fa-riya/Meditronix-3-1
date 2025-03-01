package com.example.meditronix;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.*;
import java.time.LocalDate;
import java.util.EventObject;
import java.util.Objects;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML
    private Button CreateAccount;

    @FXML
    private Button BackButton;

    @FXML
    private TextField SignUpName;
    @FXML
    private TextField FullName;
    @FXML
    private TextField Number;
    @FXML
    private TextField Email;
    @FXML
    private MenuButton GenderMenu;
    @FXML
    private PasswordField SignUpPassword;
    @FXML
    private DatePicker DateOfBirthPicker;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        SignUpName.setStyle("-fx-text-fill: #0e0707;");
        SignUpPassword.setPromptText("Enter password");
        SignUpPassword.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -30%);");
        SignUpPassword.setStyle("-fx-font-family: 'Arial';");
        SignUpPassword.setStyle("-fx-font-size: 13px;");
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    void CreateAccountPress(ActionEvent event) throws IOException {
        String username = SignUpName.getText();
        String password = SignUpPassword.getText();
        String fullname = FullName.getText();
        String emailid = Email.getText();
        String number = Number.getText();
        String gender = GenderMenu.getText();
        LocalDate dateOfBirth = DateOfBirthPicker.getValue(); // Get the selected date
        Database database = new Database();
        Connection con = database.dbConnect();

        // Check if any required fields are empty
        if (username.isEmpty() || password.isEmpty() || number.isEmpty() || fullname.isEmpty() || emailid.isEmpty() || dateOfBirth == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Please enter valid information in all fields, including Date of Birth.");
            alert.showAndWait();
            return;
        }

        if (con != null) {
            try {
                // Hash the password using SHA-256
                String hashedPassword = hashPassword(password);

                // Prepare the INSERT statement for users table
                String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'customer');";
                PreparedStatement statement = con.prepareStatement(sql);
                statement.setString(1, username);
                statement.setString(2, hashedPassword);

                // Execute the INSERT statement
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    // Insert into the `patient_info` table
                    String patientSql = "INSERT INTO patient_info (username, name, date_of_birth, gender, contact_number, email) VALUES (?, ?, ?, ?, ?, ?);";
                    PreparedStatement patientStmt = con.prepareStatement(patientSql);
                    patientStmt.setString(1, username);
                    patientStmt.setString(2, fullname);
                    patientStmt.setDate(3, java.sql.Date.valueOf(dateOfBirth)); // Convert LocalDate to SQL Date
                    patientStmt.setString(4, gender);
                    patientStmt.setString(5, number);
                    patientStmt.setString(6, emailid);
                    patientStmt.executeUpdate();

                    // Show success alert
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Your account has been created.");
                    successAlert.showAndWait();

                    // Switch to MainScreen.fxml scene
                    if (successAlert.getResult() == ButtonType.OK) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        primaryStage.setScene(scene);
                        primaryStage.show();
                    }
                } else {
                    System.out.println("Failed to insert user!");
                }
            } catch (SQLException e) {
                showErrorAlert("The username already exists, try a different username.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close the connection
                    con.close();
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        } else {
            System.out.println("Failed to connect to the database!");
        }
    }


    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    @FXML
    void ReturnPress(ActionEvent event) throws IOException{
        Object root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("MainScreen.fxml")));

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene((Parent) root);
        stage.setScene(scene);
        stage.show();
        Database db = new Database();
        db.dbConnect();
    }
    @FXML
    private void handleGenderSelect(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        GenderMenu.setText(source.getText()); // Set the MenuButton text to the selected gender
    }




}
