package com.example.meditronix;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.pdf.PdfDocument;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CustomerViewPrescription implements Initializable {

    @FXML
    private Label GenerationDate;

    @FXML
    private Button backButton;

    @FXML
    private Button downloadPDF;

    String name;
    int age;
    String gender;

    @FXML
    private Button loadPrescriptionButton;
    @FXML
    private TableView<MedicineDataPrescription> medTable;
    @FXML
    private TableColumn<MedicineDataPrescription, String> medDosageColumn;

    @FXML
    private TableColumn<MedicineDataPrescription, String> medFrequencyColumn;

    @FXML
    private TableColumn<MedicineDataPrescription, String> medNameColumn;

    @FXML
    private TableColumn<MedicineDataPrescription, Integer> medQuantityColumn;



    @FXML
    private ChoiceBox<String> prescriptionCode;

    String code;
    private Database  GlobalDB = new Database();;
    private Connection GlobalConnect =GlobalDB.dbConnect();
    String username;
    private Database database = new Database();

    public void initialize(URL url, ResourceBundle resourceBundle) {

        medNameColumn.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        medDosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        medQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        medFrequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));

        username=UserSession.getInstance().getUsername();
        try {
            // Get patient ID from username
            String patientId = GlobalDB.getPatientId(username);

            // Fetch prescriptions for the patient
            List<Prescription> prescriptions = GlobalDB.getPrescriptionsByPatientId(patientId);

            // Populate the ChoiceBox with prescription codes
            for (Prescription prescription : prescriptions) {
                prescriptionCode.getItems().add(prescription.getPrescriptionCode());
            }
            // Set up listener for the ChoiceBox
            prescriptionCode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadPrescriptionData(newValue, prescriptions);
                    code = newValue; // Update the 'code' with the selected value
                    System.out.println("Selected code: " + code);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while fetching prescriptions.");
        }
        LocalDate dateOfBirth = null;

        try {
            // Query to fetch name, gender, and date_of_birth
            String query = "SELECT name, gender, date_of_birth FROM patient_info WHERE username = ?";
            ResultSet resultSet = GlobalDB.executeQuery(query, username);

            // Process the result
            if (resultSet.next()) {
                name = resultSet.getString("name");
                gender = resultSet.getString("gender");
                dateOfBirth = resultSet.getDate("date_of_birth").toLocalDate();

                // Calculate age
                int age = Period.between(dateOfBirth, LocalDate.now()).getYears();

                // Display the details
                System.out.println("Name: " + name);
                System.out.println("Gender: " + gender);
                System.out.println("Age: " + age);
            } else {
                System.out.println("No patient found with the username: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching patient details: " + e.getMessage());
        }
    }

    @FXML
    void backButtonPressed(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PurchaseTypeSelection.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void downloadPDFPressed(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
        File file = fileChooser.showSaveDialog(downloadPDF.getScene().getWindow());

        if (file != null) {
            createPDF(file);
        }
    }


    private void loadPrescriptionData(String code, List<Prescription> prescriptions) {
        // Find the prescription based on the selected code
        Prescription selectedPrescription = prescriptions.stream()
                .filter(prescription -> prescription.getPrescriptionCode().equals(code))
                .findFirst()
                .orElse(null);

        if (selectedPrescription != null) {
            GenerationDate.setText(selectedPrescription.getGeneratedDate().toString());
        }

        // Fetch medicine data using the selected code
        ObservableList<MedicineDataPrescription> data = GlobalDB.getMedicineData(code);
        if (data == null || data.isEmpty()) {
            showAlert("No Data", "No medicine data found for the selected prescription code.");
            medTable.getItems().clear(); // Clear the table if no data is found
        } else {
            // Set the medicine data to the table
            medTable.setItems(data);
        }
    }


    private void createPDF(File file) {
        try {
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Prescription")
                    .setBold().setFontSize(18).setUnderline());
            document.add(new Paragraph("------------------------------------------------------------------------------------------------------------------------------")
                    .setBold().setFontSize(12));
            // Add patient details with specific styling
            document.add(new Paragraph("Patient Name: " + name)
                    .setBold().setFontSize(12));
            document.add(new Paragraph("Patient Age: " + age)
                    .setBold().setFontSize(12));
            document.add(new Paragraph("Patient Gender: " + gender)
                    .setBold().setFontSize(12));
            document.add(new Paragraph("Prescription Code: " + code)
                    .setBold().setFontSize(12));
            document.add(new Paragraph(" ").setMarginBottom(20)); // Add a blank line

            // Create a table for medicine details with specific styling
            Table table = new Table(4); // 4 columns
            table.addCell(new Cell().add(new Paragraph("Medicine Name")
                    .setBold().setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph("Dosage")
                    .setBold().setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph("Quantity")
                    .setBold().setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph("Frequency")
                    .setBold().setFontSize(10)));

            // Add medicine data to the table with specific styling
            for (MedicineDataPrescription med : medTable.getItems()) {
                table.addCell(new Cell().add(new Paragraph(med.getMedicineName())
                        .setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(med.getDosage())
                        .setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(med.getQuantity()))
                        .setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(med.getFrequency())
                        .setFontSize(10)));
            }

            // Add table to the document
            document.add(table);

            // Add timestamp
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = now.format(formatter);
            document.add(new Paragraph("Downloaded on: " + timestamp).setFontSize(10));

            // Close the document
            document.close();

            System.out.println("PDF created successfully");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}