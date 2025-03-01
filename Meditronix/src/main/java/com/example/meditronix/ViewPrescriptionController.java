package com.example.meditronix;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.List;

public class ViewPrescriptionController {

    private Scene scene;
    private Stage stage;
    @FXML
    private Button backButton;

    @FXML
    private TextField searchpatientnametf;

    @FXML
    private TableView<Prescription> prescodetable;

    @FXML
    private TableColumn<Prescription, String> presCodeColumn;
    @FXML
    private TableColumn<Prescription, Time> timeColumn;
    @FXML
    private TableColumn<Prescription, Date> dateColumn;

    @FXML
    private Button searchpatientbutton;
    private Database database = new Database();

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

    @FXML
    private void searchbuttonpressed(ActionEvent event) {
        String username = searchpatientnametf.getText().trim();
        System.out.println("Username: " + username);

        if (!username.isEmpty()) {
            try {
                // First get patient's ID from patient_info table
                String patientId = database.getPatientId(username);

                if (patientId != null) {
                    // Get prescriptions from patient's prescription table
                    List<Prescription> prescriptions = database.getPrescriptionsByPatientId(patientId);

                    if (!prescriptions.isEmpty()) {
                        System.out.println("Prescription Codes found for patient ID: " + patientId);
                        ObservableList<Prescription> prescriptionList = FXCollections.observableArrayList(prescriptions);
                        prescodetable.setItems(prescriptionList);
                    } else {
                        showErrorAlert("No prescriptions found for this patient.");
                    }
                } else {
                    showErrorAlert("No patient found with username: " + username);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Database error: " + e.getMessage());
            }
        } else {
            showErrorAlert("Please enter patient's username.");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        presCodeColumn.setCellValueFactory(new PropertyValueFactory<>("prescriptionCode"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("generatedDate"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("generatedTime"));



        // Add a listener to handle row selection
        prescodetable.setRowFactory(tv -> {
            TableRow<Prescription> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Prescription rowData = row.getItem();
                    String prescriptionCode = rowData.getPrescriptionCode();
                    openPrescriptionScene(prescriptionCode);
                }
            });
            return row;
        });

        // Add username suggestions
        searchpatientnametf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    List<String> suggestions = database.getUsernameSuggestions(newValue);
                    showSuggestions(suggestions);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                hideSuggestions();
            }
        });
    }

    private void openPrescriptionScene(String prescriptionCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("IndividualPrescription.fxml"));
            Parent root = loader.load();

            IndividualPrescription controller = loader.getController();
            controller.setPrescriptionCode(prescriptionCode);

            Scene scene = new Scene(root);
            Stage stage = (Stage) prescodetable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSuggestions(List<String> suggestions) {
        // Create a new ListView to display the suggestions
        ListView<String> suggestionsListView = new ListView<>();
        suggestionsListView.getItems().addAll(suggestions);

        // Set the size and position of the ListView
        suggestionsListView.setPrefWidth(searchpatientnametf.getWidth());
        suggestionsListView.setPrefHeight(100);
        suggestionsListView.setLayoutX(searchpatientnametf.getLayoutX());
        suggestionsListView.setLayoutY(searchpatientnametf.getLayoutY() + searchpatientnametf.getHeight());

        // Add an event handler to handle selection of a suggestion
        suggestionsListView.setOnMouseClicked(event -> {
            String selectedSuggestion = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selectedSuggestion != null) {
                searchpatientnametf.setText(selectedSuggestion);
                hideSuggestions();
            }
        });

        // Add the ListView to the scene
        ((Pane) searchpatientnametf.getParent()).getChildren().add(suggestionsListView);
    }

    private void hideSuggestions() {
        // Remove the ListView from the scene
        ((Pane) searchpatientnametf.getParent()).getChildren().removeIf(node -> node instanceof ListView);
    }


}
