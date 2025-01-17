package com.example.meditronix;

import java.sql.Connection;

public class MedicineDataPrescription {
    private String medicineName;
    private String dosage;
    private int quantity;
    private String frequency;

    public MedicineDataPrescription(String medicineName, String dosage, int quantity, String frequency) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.quantity = quantity;
        this.frequency = frequency;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    //return expiry date of medicine at the selected location

}