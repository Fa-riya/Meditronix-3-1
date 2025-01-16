package com.example.meditronix;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;


public class CreatePrescriptionController {

    private Stage stage;
    private Scene scene;


    @FXML
    private TextField Dosage;

    @FXML
    private TextField Frequency;

    @FXML
    private TextField MedicineName;


    @FXML
    private TextField Quantity;

    @FXML
    private Button addMedicineButton;

    @FXML
    private Button backButton;


    @FXML
    private Button PrescriptionCode;

    @FXML
    private TextField medicineDosage;

    @FXML
    private VBox medicineEntryTemplate;

    @FXML
    private TextField medicineFrequency;

    @FXML
    private TextField medicineName;

    @FXML
    private TextField medicineQuantity;

    @FXML
    private Button removeMedicineButton;

    @FXML
    private Button stopPrescriptionButton;

    @FXML
    private Button removePatientButton;

    @FXML
    private Label MedicineCountLabel;

    @FXML
    private TableView<Medicine> MedicineTableview;

    @FXML
    private TableColumn<Medicine, String> mednamecol;

    @FXML
    private TableColumn<Medicine, String> meddosagecol;

    @FXML
    private TableColumn<Medicine, String> medquantitycol;

    @FXML
    private TableColumn<Medicine, String> medfreqcol;

    private List<Medicine> medicines = new ArrayList<>();


    private String prescriptionCode;

    private Database database = new Database();

    private int medicineCount = 0;
    private static final int MAX_MEDICINES = 15;

    private Random random = new Random();

    private String name;
    private String ageText;
    private String gender;

    private String contact;

    private String email;

    @FXML
    private Label enteredPatientAge;

    @FXML
    private Label enteredPatientGender;

    @FXML
    private Label enteredPatientName;

    @FXML
    private Label enteredUsername;

    private boolean patientHasCreated = false;

    @FXML
    private TextField usernameTf;

    private String username;

    @FXML
    private Button Add;


    @FXML
    private Label enteredPatientContact;

    @FXML
    private Label enteredPatientEmail;

    @FXML
    private Button remove;




    LocalDateTime now = LocalDateTime.now();

    private String generatePrescriptionCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    private String getPresCode()
    {
        return prescriptionCode;
    }

    //16-1-25-rumman (lines 160-203)

    @FXML
    void usernameTfPressed(ActionEvent event) {
        username = usernameTf.getText().trim();

        if (username.isEmpty()) {
            showErrorAlert("Please enter a username.");
            return;
        }

        try {
            Database.PatientInfo patientInfo = database.getPatientInfoByUsername(username);

            if (patientInfo != null) {
                // Update the UI labels with patient information
                enteredUsername.setText(username);
                enteredPatientName.setText(patientInfo.getName());
                enteredPatientAge.setText(String.valueOf(patientInfo.getAge()));
                enteredPatientGender.setText(patientInfo.getGender());
                enteredPatientContact.setText(patientInfo.getContact());
                enteredPatientEmail.setText(patientInfo.getEmail());

                // Store the values for later use
                name = patientInfo.getName();
                ageText = String.valueOf(patientInfo.getAge());
                gender = patientInfo.getGender();
                contact = patientInfo.getContact();
                email = patientInfo.getEmail();

                patientHasCreated = true;
                // Remove the automatic disabling of Add button
                // Add.setDisable(true);

            } else {
                showErrorAlert("No patient found with this username.");
                //clearPatientLabels();
                patientHasCreated = false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database error: " + e.getMessage());
            //clearPatientLabels();
            patientHasCreated = false;
        }
    }



    @FXML
    void addMedicine(ActionEvent event) {
        String name = MedicineName.getText();
        String dosage = Dosage.getText();
        String quantity = Quantity.getText();
        String frequency = Frequency.getText();

        if (validateMedicineFields(name, dosage, quantity, frequency)) {
            if (medicineCount < MAX_MEDICINES) {
                Medicine medicine = new Medicine(name, dosage, quantity, frequency);
                medicines.add(medicine);
                medicineCount++;

                MedicineCountLabel.setText("Medicine Count: " + medicineCount);


                MedicineTableview.getItems().add(medicine);

                System.out.println("Added medicine: " + medicine);
            } else {
                showErrorAlert("Maximum number of medicines reached.");
            }
        } else {
            showErrorAlert("Please fill in all medicine details.");
        }

        clearMedicineFields();
    }

    private boolean validateMedicineFields(String name, String dosage, String quantity, String frequency) {
        return !name.isEmpty() && !dosage.isEmpty() && !quantity.isEmpty() && !frequency.isEmpty();
    }


    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotification(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    void addPatient(ActionEvent event) {
        String username = usernameTf.getText().trim();

        if (username.isEmpty()) {
            showErrorAlert("Please enter a username first.");
            return;
        }

        try {
            Database.PatientInfo patientInfo = database.getPatientInfoByUsername(username);

            if (patientInfo != null) {
                // Update the UI labels with new patient information
                enteredUsername.setText(username);
                enteredPatientName.setText(patientInfo.getName());
                enteredPatientAge.setText(String.valueOf(patientInfo.getAge()));
                enteredPatientGender.setText(patientInfo.getGender());
                enteredPatientContact.setText(patientInfo.getContact());
                enteredPatientEmail.setText(patientInfo.getEmail());


                // Store the new values
                name = patientInfo.getName();
                ageText = String.valueOf(patientInfo.getAge());
                gender = patientInfo.getGender();
                contact = patientInfo.getContact();
                email = patientInfo.getEmail();

                patientHasCreated = true;

                showNotification("Patient information updated successfully.");
            } else {
                showErrorAlert("No patient found with this username.");
                //clearPatientLabels();
                patientHasCreated = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database error: " + e.getMessage());
            //clearPatientLabels();
            patientHasCreated = false;
        }
    }



    @FXML
    void PrescriptionCodePressed(ActionEvent event) {
        if (prescriptionCode == null) {
            showErrorAlert("No prescription has been created yet.");
        } else {
            // Create a custom dialog pane
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Prescription Code");
            dialog.setHeaderText(null);


            Label codeLabel = new Label("Prescription Code: " + prescriptionCode.toLowerCase());

            Label instructionLabel = new Label("Click the 'Copy' button to copy the code to clipboard.");

            Button copyButton = new Button("Copy");
            copyButton.setOnAction(e -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(prescriptionCode);
                clipboard.setContent(content);
                dialog.close();
            });

            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(codeLabel, instructionLabel, copyButton);

            dialog.getDialogPane().setContent(vbox);

            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();
        }
    }




    private void showCopyDialog(String title, String content) {
        // Create a custom dialog pane
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        Label contentLabel = new Label(content);
        Label instructionLabel = new Label("Click the 'Copy' button to copy the content to clipboard.");

        Button copyButton = new Button("Copy");
        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
            dialog.close();
        });

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(contentLabel, instructionLabel, copyButton);

        dialog.getDialogPane().setContent(vbox);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }





    @FXML
    void backButtonPressed(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DoctorMenu.fxml"));
            Parent root = fxmlLoader.load();
            scene = new Scene(root);
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearMedicineFields() {
        MedicineName.clear();
        Dosage.clear();
        Quantity.clear();
        Frequency.clear();
    }

    @FXML
    void medicineNameTfPressed(ActionEvent event) {
        String medicineNameText = MedicineName.getText();
        if (!medicineNameText.matches("[a-zA-Z0-9 ]+")) {
            showErrorAlert("Medicine name should only contain letters, numbers, and spaces.");
            MedicineName.clear();
        } else {
            System.out.println("Medicine Name: " + medicineNameText);
            hideSuggestions();
        }
    }

    @FXML
    void dosageTfPressed(ActionEvent event) {
        String dosageText = Dosage.getText();
        if (!dosageText.matches("[a-zA-Z0-9 ]+")) {
            showErrorAlert("Dosage should only contain letters, numbers, and spaces.");
            Dosage.clear();
        } else {
            System.out.println("Dosage: " + dosageText);
        }
    }

    @FXML
    void quantityTfPressed(ActionEvent event) {
        String quantityText = Quantity.getText();
        if (!quantityText.matches("\\d+")) {
            showErrorAlert("Quantity should only contain integers.");
            Quantity.clear();
        } else {
            System.out.println("Quantity: " + quantityText);
        }
    }


    @FXML
    void frequencyTfPressed(ActionEvent event) {
        String frequencyText = Frequency.getText();
        if (frequencyText.isEmpty()) {
            showErrorAlert("Frequency cannot be empty.");
            Frequency.clear();
        } else {
            System.out.println("Frequency: " + frequencyText);
        }
    }





    @FXML
    void removeMedicine(ActionEvent event) {
        // get the selected medicine
        Medicine selectedMedicine = MedicineTableview.getSelectionModel().getSelectedItem();
        if (selectedMedicine != null) {

            MedicineTableview.getItems().remove(selectedMedicine);

            medicines.remove(selectedMedicine);

            if(medicineCount > 0) {
                medicineCount--;
            }

            MedicineCountLabel.setText("Medicine Count: " + medicineCount);
            database.deleteMedicineData(prescriptionCode, selectedMedicine.getName(), selectedMedicine.getDosage(), selectedMedicine.getQuantity(), selectedMedicine.getFrequency());
        } else {
            System.out.println("No medicine selected to remove.");
        }
    }


    @FXML
    void stopPrescription(ActionEvent event) {
        if (medicines.isEmpty()) {
            showErrorAlert("Please add medicine before creating a prescription.");
            return;
        }
        else if(enteredPatientEmail.getText()== "") {
            showErrorAlert("Please add patient info before making prescription.");
            return;
        }

        try {
            // Generate prescription code
            prescriptionCode = generatePrescriptionCode();

            // Get patient's ID from patient_info table
            String patientId = database.getPatientId(usernameTf.getText().trim());
            if (patientId == null) {
                showErrorAlert("Could not retrieve patient ID.");
                return;
            }

            // Create patient-specific prescription history table and add new prescription
            database.addPrescriptionToPatientHistory(patientId, prescriptionCode, now, username);

            showNotification("Prescription has been created. Prescription Code: " + prescriptionCode.toLowerCase());
            System.out.println("Prescription Code: " + prescriptionCode.toLowerCase());

            // Create new table for prescription and insert patient data
            database.createPrescriptionTable(prescriptionCode, name, now, ageText, gender);

            // Insert medicine data into table
            for (Medicine medicine : medicines) {
                try {
                    int quantity = Integer.parseInt(medicine.getQuantity());
                    database.insertMedicineData(prescriptionCode, medicine.getName(),
                            medicine.getDosage(), quantity, medicine.getFrequency(), now);
                } catch (NumberFormatException e) {
                    showErrorAlert("Quantity must be a valid integer.");
                }
            }

            medicines.clear();
            medicineCount = 0;
            if (medicineCount >= 0) {
                MedicineCountLabel.setText("Medicine Count: " + medicineCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error creating prescription: " + e.getMessage());
        }
    }



    public class Medicine {
        public String name;
        public String dosage;
        public String quantity;
        public String frequency;

        public Medicine(String name, String dosage, String quantity, String frequency) {
            this.name = name;
            this.dosage = dosage;
            this.quantity = quantity;
            this.frequency = frequency;
        }

        public String getName() {
            return name;
        }

        public String getDosage() {
            return dosage;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getFrequency() {
            return frequency;
        }

        @Override
        public String toString() {
            return "Medicine{" +
                    "name='" + name + '\'' +
                    ", dosage='" + dosage + '\'' +
                    ", quantity='" + quantity + '\'' +
                    ", frequency='" + frequency + '\'' +
                    '}';
        }
    }

    private List<String> getSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();

        try (Connection con = database.dbConnect()) {
            ResultSet rs = database.searchByName(con, input);
            while (rs.next()) {
                String name = rs.getString("Name");
                suggestions.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suggestions;
    }

    private void showSuggestions(List<String> suggestions) {
        // Create a new ListView to display the suggestions
        ListView<String> suggestionsListView = new ListView<>();
        suggestionsListView.getItems().addAll(suggestions);

        // Set the size and position of the ListView
        suggestionsListView.setPrefWidth(MedicineName.getWidth());
        suggestionsListView.setPrefHeight(100);
        suggestionsListView.setLayoutX(MedicineName.getLayoutX());
        suggestionsListView.setLayoutY(MedicineName.getLayoutY() + MedicineName.getHeight());

        // Add an event handler to handle selection of a suggestion
        suggestionsListView.setOnMouseClicked(event -> {
            String selectedSuggestion = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selectedSuggestion != null) {
                MedicineName.setText(selectedSuggestion);
                hideSuggestions();
            }
        });

        // Add the ListView to the scene
        ((Pane) MedicineName.getParent()).getChildren().add(suggestionsListView);
    }

    private void hideSuggestions() {
        // Remove the ListView from the scene
        ((Pane) MedicineName.getParent()).getChildren().removeIf(node -> node instanceof ListView);
    }

    private void showDosageSuggestions(List<String> dosages) {
        ContextMenu contextMenu = new ContextMenu();
        for (String dosage : dosages) {
            MenuItem item = new MenuItem(dosage);
            item.setOnAction(e -> Dosage.setText(dosage));
            contextMenu.getItems().add(item);
        }
        Dosage.setContextMenu(contextMenu);
        contextMenu.show(Dosage, Side.BOTTOM, 0, 0);
    }

    private void hideDosageSuggestions() {
        if (Dosage.getContextMenu() != null) {
            Dosage.getContextMenu().hide();
        }
    }

    private List<String> getUsernameSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();
        try (Connection con = database.dbConnect()) {
            String query = "SELECT username FROM patient_info WHERE username LIKE '" + input + "%'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String username = rs.getString("username");
                suggestions.add(username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    private ListView<String> suggestionsListView;

    private void showUsernameSuggestions(List<String> suggestions) {
        if (suggestionsListView != null) {
            ((Pane) usernameTf.getParent()).getChildren().remove(suggestionsListView);
        }

        suggestionsListView = new ListView<>();
        suggestionsListView.setItems(FXCollections.observableArrayList(suggestions));

        suggestionsListView.setPrefWidth(usernameTf.getWidth());
        suggestionsListView.setPrefHeight(Math.min(100, suggestions.size() * 24)); // 24 pixels per item
        suggestionsListView.setLayoutX(usernameTf.getLayoutX());
        suggestionsListView.setLayoutY(usernameTf.getLayoutY() + usernameTf.getHeight());

        // Add mouse click handler
        suggestionsListView.setOnMouseClicked(event -> {
            String selectedSuggestion = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selectedSuggestion != null) {
                usernameTf.setText(selectedSuggestion);
                hideUsernameSuggestions();
                // Trigger the username search
                ActionEvent actionEvent = new ActionEvent(usernameTf, null);
                usernameTfPressed(actionEvent);
                // Return focus to the text field
                usernameTf.requestFocus();
            }
        });

        // Add keyboard navigation
        suggestionsListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String selectedSuggestion = suggestionsListView.getSelectionModel().getSelectedItem();
                if (selectedSuggestion != null) {
                    usernameTf.setText(selectedSuggestion);
                    hideUsernameSuggestions();
                    // Trigger the username search
                    ActionEvent actionEvent = new ActionEvent(usernameTf, null);
                    usernameTfPressed(actionEvent);
                    // Return focus to the text field
                    usernameTf.requestFocus();
                }
            }
        });

        ((Pane) usernameTf.getParent()).getChildren().add(suggestionsListView);

        // Keep focus on the text field
        usernameTf.requestFocus();
    }

    private void hideUsernameSuggestions() {
        if (suggestionsListView != null) {
            ((Pane) usernameTf.getParent()).getChildren().remove(suggestionsListView);
            suggestionsListView = null;
        }
    }

    @FXML
    public void initialize() {
        // ComboBox options
        //GenderCombobox.getItems().addAll("Male", "Female");
        //GenderCombobox.getSelectionModel().selectFirst();

        // medicine TableView
        mednamecol.setCellValueFactory(new PropertyValueFactory<>("name"));
        meddosagecol.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        medquantitycol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        medfreqcol.setCellValueFactory(new PropertyValueFactory<>("frequency"));

        TextFormatter<String> medicineNameFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[a-zA-Z0-9 ]+")) {
                return change;
            }
            return null;
        });
        //MedicineName.setTextFormatter(medicineNameFormatter);

        MedicineName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                List<String> suggestions = getSuggestions(newValue);
                showSuggestions(suggestions);
            } else {
                hideSuggestions();
            }
        });

        Dosage.textProperty().addListener((observable, oldValue, newValue) -> {
            String medicineName = MedicineName.getText();
            if (!medicineName.isEmpty()) {
                List<String> dosages = database.getDosagesByMedicineName(medicineName);
                showDosageSuggestions(dosages);
            }
            else {
                hideDosageSuggestions();
            }
        });

        usernameTf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                List<String> suggestions = getUsernameSuggestions(newValue);
                if (!suggestions.isEmpty()) {
                    showUsernameSuggestions(suggestions);
                } else {
                    hideUsernameSuggestions();
                }
            } else {
                hideUsernameSuggestions();
            }
            // Ensure text field keeps focus
            usernameTf.requestFocus();
        });

        // Update focus listener for username TextField
        usernameTf.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {  // If focus is lost
                // Add a small delay before hiding suggestions to allow for mouse clicks
                javafx.application.Platform.runLater(() -> {
                    if (suggestionsListView != null &&
                            !suggestionsListView.isFocused() &&
                            !suggestionsListView.isHover()) {
                        hideUsernameSuggestions();
                    }
                });
            }
        });

        // Add keyboard navigation for username TextField
        usernameTf.setOnKeyPressed(event -> {
            if (suggestionsListView != null) {
                if (event.getCode() == KeyCode.DOWN) {
                    suggestionsListView.requestFocus();
                    suggestionsListView.getSelectionModel().select(0);
                }
            }
        });

    }

}

