package com.example.meditronix;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.sql.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.List;

public class PrescribedPurchase implements Initializable {

    public static final String BASE_FILE_PATH = "F:\\Sem5\\RDBMS project\\Meditronix-3-1\\Meditronix\\memos\\";

    String code ="";
    @FXML
    private TableView<Medicine> CartTable;
    @FXML
    private TableColumn<Medicine, String> CartNameColumn;
    @FXML
    private TableColumn<Medicine, String> CartDosageColumn;
    @FXML
    private TableColumn<Medicine, Float> CartQuantityColumn;
    @FXML
    private TableColumn<Medicine, Float> CartPriceColumn;

    ObservableList<Medicine> cartList = FXCollections.observableArrayList();

    @FXML
    private ChoiceBox<String> PresCode;
    @FXML
    private ChoiceBox<String> Location;
    private String selectedLocation = "";
    private String username;
    @FXML
    private Button addButton;

    @FXML
    private Button checkoutButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button BackButton;

    @FXML
    private TextField Quantity;

    @FXML
    private Label subtotalLabel;
    @FXML
    private Label GenerationDate;

    @FXML
    private TableView<MedicineDataPrescription> PresTable;
    @FXML
    private TableColumn<MedicineDataPrescription, String> nameColumn;
    @FXML
    private TableColumn<MedicineDataPrescription, String> dosageColumn;
    @FXML
    private TableColumn<MedicineDataPrescription, Integer> quantityColumn;
    @FXML
    private TableColumn<MedicineDataPrescription, String> frequencyColumn;

    private Database GlobalDB;
    private Connection GlobalConnect;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateLocations();
        GlobalDB = new Database();
        GlobalConnect = GlobalDB.dbConnect();
        CartNameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        CartDosageColumn.setCellValueFactory(new PropertyValueFactory<>("Dose"));
        CartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("Quantity"));
        CartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("Price"));

        CartTable.setItems(cartList);

        setupLocationListener();
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        frequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        username=UserSession.getInstance().getUsername();
        try {
            // Get patient ID from username
            String patientId = GlobalDB.getPatientId(username);

            // Fetch prescriptions for the patient
            List<Prescription> prescriptions = GlobalDB.getPrescriptionsByPatientId(patientId);

            // Populate the ChoiceBox with prescription codes
            for (Prescription prescription : prescriptions) {
                PresCode.getItems().add(prescription.getPrescriptionCode());
            }
            // Set up listener for the ChoiceBox
            PresCode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
    }
    private void setupLocationListener() {
        Location.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedLocation = newValue;
            }
        });
    }

    public void detectStockEmpty()
    {
        PresTable.setRowFactory(tv -> new TableRow<MedicineDataPrescription>() {
            @Override
            protected void updateItem(MedicineDataPrescription item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    //Set priority with higher priority given to expiry date,then to stock level
                    // If quantity is 0, color the row red
                    if(Database.isMedicineExpired(item.getMedicineName(), item.getDosage(),Location.getValue(),GlobalConnect)){
                        setStyle("-fx-background-color: #c94059;-fx-font-weight: bold;");
                    }
                    else if (item.getQuantity() == 0) {


                        setStyle("-fx-background-color: #8a61bd;-fx-font-weight: bold;");
                    }
                    else {

                        setStyle(""); // Default style
                    }
                }
            }
        });
    }


    @FXML
    private void loadPrescriptionData(String code, List<Prescription> prescriptions) {
        // Find the prescription and update the generation date
        Prescription selectedPrescription = prescriptions.stream()
                .filter(prescription -> prescription.getPrescriptionCode().equals(code))
                .findFirst()
                .orElse(null);

        if (selectedPrescription != null) {
            GenerationDate.setText(selectedPrescription.getGeneratedDate().toString());
        }

        // Fetch and display medicine data
        ObservableList<MedicineDataPrescription> data = GlobalDB.getMedicineData(code);
        if (data == null || data.isEmpty()) {
            showError("Invalid prescription code, no data found.");
            PresTable.getItems().clear(); // Clear the table if no data is found
        } else {
            PresTable.setItems(data);
            detectStockEmpty();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void addButtonPressed(ActionEvent event) {
        try {
            float quantityToAdd = Float.parseFloat(Quantity.getText().trim());

            // Check if quantityToAdd is greater than 0
            if (quantityToAdd <= 0) {
                showError("Quantity must be greater than 0.");
                return;
            }
            MedicineDataPrescription selectedMedicine = PresTable.getSelectionModel().getSelectedItem();
            if (selectedMedicine == null) {
                showError("No medicine selected in the table.");
                return;
            }
            try (Connection con = GlobalDB.dbConnect()) {
                if (GlobalDB.isMedicineExpired(selectedMedicine.getMedicineName(), selectedMedicine.getDosage(),selectedLocation, con)) {
                    showError("The selected medicine is expired.");
                    return;
                }
            }
            if (quantityToAdd <= selectedMedicine.getQuantity()) {
                float availableQuantity = GlobalDB.getMedicineQuantity( selectedMedicine.getMedicineName(),selectedMedicine.getDosage(),selectedLocation);
                if (quantityToAdd <= availableQuantity) {
                  // System.out.println("Medicine Name: " + selectedMedicine.getMedicineName());
                    //System.out.println("Dosage: " + selectedMedicine.getDosage());
                   // System.out.println("Quantity to Add: " + quantityToAdd);
                 //   System.out.println("Location " + selectedLocation);
                  //  System.out.println("Code " + code);

                    float price = GlobalDB.updateMedicineQuantity(code, selectedMedicine.getMedicineName(), selectedMedicine.getDosage(), quantityToAdd,selectedLocation);
                    Medicine newMedicine = new Medicine(selectedMedicine.getMedicineName(), selectedMedicine.getDosage(), quantityToAdd, price);

                    // Add the new Medicine object to the cartTable
                    cartList.add(newMedicine);
                    selectedMedicine.setQuantity((int) (selectedMedicine.getQuantity() - quantityToAdd));
                    // Refresh both PresTable and CartTable
                    PresTable.refresh();
                    CartTable.setItems(cartList);
                    CartTable.refresh();
                    calculateAndSetSubtotal();

                } else {
                    showError("The entered quantity exceeds the available quantity of the selected medicine in the inventory.");
                }


            } else {
                showError("The entered quantity exceeds your purchase limit of the selected medicine.");
            }
        } catch (NumberFormatException e) {
            showError("Invalid quantity. Please enter a valid float number.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void calculateAndSetSubtotal() {
        float subtotal = 0.0f;
        for (Medicine medicine : cartList) {
            subtotal += medicine.getQuantity() * medicine.getPrice();
        }
        // Update the subtotal label with the calculated subtotal
        subtotalLabel.setText(String.format("Subtotal:        %.2f   Tk", subtotal));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void checkoutButtonPressed(ActionEvent event) throws IOException {
        try {
            // Step 1: Call addMemo function
            boolean memoAdded = GlobalDB.addMemo();

            if (memoAdded) {
                // Step 2: Get the last memo value
                int lastMemoValue = GlobalDB.getLastMemoValue();

                // Step 3: Generate the PDF
                generatePdfForCart(lastMemoValue);

                showCheckoutSuccessAlert("Checkout Successful", "Your cart has been checked out and a memo has been generated.");
            } else {
                showAlert("Checkout Failed", "Failed to add memo. Please try again.");
            }
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during checkout. Please try again.");
        }

        //Back to previous menu
        Object root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("PurchaseTypeSelection.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene((Parent) root);
        stage.setScene(scene);
        stage.show();
    }



    public void showCheckoutSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText("Thank You For Your Purchase");
        alert.showAndWait();
    }
    private void generatePdfForCart(int memoNo) throws FileNotFoundException {
        String dest = BASE_FILE_PATH + memoNo + ".pdf";
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Add heading "Meditronix"
            Paragraph heading = new Paragraph("Meditronix")
                    .setFont(boldFont)
                    .setFontSize(48)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(heading);

            // Get current date and time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);

            // Add title "Memo No: <memoNo>" and current date and time
            Paragraph title = new Paragraph("Invoice No: " + memoNo + "\nTime: " + formattedDateTime+ "\nBranch: " + Location.getValue())
                    .setFont(regularFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(20);
            document.add(title);

            // Create a table with the same columns as the cartTable
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            // Add header row with custom formatting
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Name").setFont(boldFont).setFontSize(12).setPadding(5)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Dose").setFont(boldFont).setFontSize(12).setPadding(5)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Quantity").setFont(boldFont).setFontSize(12).setPadding(5)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Price").setFont(boldFont).setFontSize(12).setPadding(5)).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // Calculate subtotal
            float subtotal = 0;

            // Add rows from cartList with custom formatting
            for (Medicine medicine : cartList) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(medicine.getName()).setFont(regularFont).setFontSize(12).setPadding(5)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(medicine.getDose()).setFont(regularFont).setFontSize(12).setPadding(5)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(medicine.getQuantity().toString()).setFont(regularFont).setFontSize(12).setPadding(5)));
                table.addCell(new Cell().add(new Paragraph(medicine.getPrice().toString()).setFont(regularFont).setFontSize(12).setPadding(5)));
                subtotal += medicine.getQuantity() * medicine.getPrice();
            }

            // Add table to document
            document.add(table);

            // Add subtotal to document
            Paragraph subtotalParagraph = new Paragraph("Subtotal: " + subtotal + "  Tk")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(subtotalParagraph);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close document
            document.close();
        }

        String filePath = BASE_FILE_PATH + memoNo + ".pdf";
        try {
            File file = new File(filePath);

            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("The specified file does not exist.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void DeleteButtonPressed(ActionEvent event) throws SQLException {
        Medicine medInCart = CartTable.getSelectionModel().getSelectedItem();

        if (medInCart == null) {
            showAlert("No Selection", "No medicine selected in the cart. Please select a medicine to remove.");
            return;
        }

        cartList.remove(medInCart);
        // Refresh the cart table
        CartTable.refresh();
        // Calculate and set the subtotal
        calculateAndSetSubtotal();
        if (!GlobalDB.removeMedFromCart(code, medInCart.getName(), medInCart.getDose(), medInCart.getQuantity(),selectedLocation)) {
            showAlert("Database Error", "Failed to remove the medicine from the cart in the database.");
            return;
        }
        // Call the method to retrieve updated data for PresTable from the database
        ObservableList<MedicineDataPrescription> data = GlobalDB.getMedicineData(code);

        // Set the updated data to the PresTable
        PresTable.setItems(data);
        PresTable.refresh();

        calculateAndSetSubtotal();

    }
    @FXML
    void BackButtonPressed(ActionEvent event) throws IOException {

        // Iterate over each medicine in the cartList
        for (Medicine medInCart : cartList) {
            try {
                // Remove the selected medicine from the database
                if (!GlobalDB.removeMedFromCart(code, medInCart.getName(), medInCart.getDose(), medInCart.getQuantity(),selectedLocation)) {
                    showAlert("Database Error", "Failed to remove the medicine from the cart in the database.");
                    return;
                }

            } catch (SQLException e) {
                showAlert("Database Error", "An error occurred while accessing the database: " + e.getMessage());
                e.printStackTrace(); // Print the stack trace for debugging
            }
        }
        Object root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("PurchaseTypeSelection.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene((Parent) root);
        stage.setScene(scene);
        stage.show();
    }
    private void populateLocations() {
        Database database = new Database(); // Assuming you have a Database class to handle connections
        Connection con = database.dbConnect();

        if (con != null) {
            try {
                String query = "SELECT DISTINCT store_location FROM shop_inventory WHERE store_location IS NOT NULL";
                PreparedStatement stmt = con.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                // Add unique locations to the ChoiceBox
                while (rs.next()) {
                    String location = rs.getString("store_location");
                    Location.getItems().add(location);
                }

                // Set default placeholder text
                Location.setValue("Select Location");

                // Set an action listener to handle selection changes
                Location.setOnAction(event -> {
                    String selectedLocation = Location.getValue();
                    System.out.println("Selected Location: " + selectedLocation);
                    // You can store or use the selected location as needed

                    // On location changes, set cart to clear to avoid purchase of med from multiple location under one memo
                    // This is done to avoid storing location info in memo
                    // ----Added by Rafid
                    cartList.clear();
                    CartTable.refresh();
                    detectStockEmpty();
                });

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Location Error","Error fetching locations: " + e.getMessage());
            } finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Failed to connect to the database!");
        }
    }
}
