package com.example.meditronix;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import org.controlsfx.control.textfield.TextFields;

public class AddMedicinePanel extends Pane implements Initializable {
    public Button addButton;

    @FXML
    private TextField BuyCostField;

    @FXML
    private DatePicker expiryDateField;

    @FXML
    private TextField QuantityField;

    @FXML
    private TextField SellCostField;


    @FXML
    private Spinner<String> addPanelUnitSelector;

    @FXML
    private ChoiceBox<String> typeList;

    @FXML
    private HBox addHboxPanel;

    @FXML
    private AnchorPane addPanel;

    @FXML
    private TextField doseField;

    @FXML
    private TextField nameField;

    @FXML
    private Label warningLabel;
    private Stage stage;

    private String selectedType;

    private TextFields genericDrugAutoComplete;

    @FXML
    private TextField drugGenericNameField;

    private Connection con;
    private Database localDB;

    private final String[] types = {"Prescription","Generic"};

    public void show() {
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }

    private final String[] units = {"","g","mg","ug","L","mL"};
    ObservableList<String> Units = FXCollections.observableArrayList(
            Arrays.asList(GenericDrugsList.dosageUnits)
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        SpinnerValueFactory<String> valueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(
                Units
        );
        // Set the factory to the spinner
        addPanelUnitSelector.setValueFactory(valueFactory);
        // Set the default value (optional)
        addPanelUnitSelector.getValueFactory().setValue(Units.get(0));
        // Add change listener
        addPanelUnitSelector.valueProperty().addListener((obs, oldValue, newValue) -> {
            // Your action here
            System.out.println("Selected unit: " + newValue);
        });

          TextFields.bindAutoCompletion(drugGenericNameField,GenericDrugsList.genericDrugNames);
          typeList.getItems().addAll(types);
          typeList.setOnAction(this::returnType);
          localDB = ShopMenu.getInstance().getGlobalDB();
          con = ShopMenu.getInstance().getConnection();


          BuyCostField.setText("0.0");
          doseField.setText("-");


    }

    public void returnType(ActionEvent event)
    {
        selectedType = typeList.getValue();
    }

    @FXML
    public void addToInventory(ActionEvent event) throws SQLException {




           Float buyingCost, sellingCost, quantityAdded;
           String name, type, dose,drugGenericName;
           LocalDate expiryDate;
           String date;

           //Set with default values if field is not completed


           // expiryDate = LocalDate.parse("31/12/2100");

           //medicine class takes in expiry date as a string, so format LocalDate to a string
           if (expiryDateField.getValue() != null) {
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
               date = expiryDateField.getValue().format(formatter);
           } else {
               expiryDateField.setValue(LocalDate.of(2100, 12, 31));
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
               date = expiryDateField.getValue().format(formatter);
           }
           if (BuyCostField.getText() != null) {
               buyingCost = Float.valueOf(BuyCostField.getText());
           } else {
               BuyCostField.setText("0.0");
               buyingCost = Float.valueOf(BuyCostField.getText());
           }
           if (doseField.getText() != null) {
               dose = doseField.getText();
               dose = dose + addPanelUnitSelector.getValue();
           } else {
               doseField.setText("_");
               dose = doseField.getText();
           }

           if (nameField.getText() != null && typeList.getValue() != null &&
                   SellCostField.getText() != null && QuantityField.getText() != null) {
               //Must fill up these fields
               name = nameField.getText();
               type = typeList.getValue();
               sellingCost = Float.valueOf(SellCostField.getText());
               quantityAdded = Float.valueOf(QuantityField.getText());
               drugGenericName = drugGenericNameField.getText();

               Medicine medicine = new Medicine(name,drugGenericName, dose, date, type, sellingCost, quantityAdded, buyingCost);


               localDB.addMedicine(medicine, con,warningLabel);

           } else {
               warningLabel.setText("Fields with * must be filled!");

               warningLabel.setVisible(true);

               // Create a Timeline to hide the label after 3 seconds
               Timeline timeline = new Timeline(
                       new KeyFrame(Duration.seconds(3), new EventHandler<ActionEvent>() {
                           @Override
                           public void handle(ActionEvent event) {
                               // Hide the warning label after the specified duration
                               warningLabel.setVisible(false);
                           }
                       })
               );

               // Play the timeline once to hide the label after 3 seconds
               timeline.setCycleCount(1);
               timeline.play();
           }

           ShopMenu.getInstance().refreshList();



    }




}
