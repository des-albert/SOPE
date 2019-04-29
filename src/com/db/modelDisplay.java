package com.db;

import javafx.beans.property.SimpleStringProperty;

public class modelDisplay {
    private final SimpleStringProperty modModelId;
    private final SimpleStringProperty modNumber;
    private final SimpleStringProperty modDescription;
    private final SimpleStringProperty modExtDesc;

    modelDisplay(String id, String number, String Description, String extDesc) {

        this.modModelId = new SimpleStringProperty(id);
        this.modNumber = new SimpleStringProperty(number);
        this.modDescription = new SimpleStringProperty(Description);
        this.modExtDesc = new SimpleStringProperty(extDesc);
    }
    public String getModModelId () {return modModelId.get();}
    public String getModNumber () {return modNumber.get();}
    public String getModDescription () {return modDescription.get();}
    public String getModExtDesc () {return modExtDesc.get();}
}
