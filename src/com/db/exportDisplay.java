package com.db;

import javafx.beans.property.SimpleStringProperty;

public class exportDisplay {
    private final SimpleStringProperty exParentId;
    private final SimpleStringProperty exPartType;
    private final SimpleStringProperty exDescription;
    private final SimpleStringProperty exProductLine;
    private final SimpleStringProperty exCognizant;
    private final SimpleStringProperty exUoM;
    private final SimpleStringProperty exBoM_Line;
    private final SimpleStringProperty exPart_Number;
    private final SimpleStringProperty exBoM_Qty;
    private final SimpleStringProperty exBoM_Class;
    private final SimpleStringProperty exAsset;
    private final SimpleStringProperty exModel;
    private final SimpleStringProperty exExtDesc;
    private final SimpleStringProperty exConfigType;

    exportDisplay(String parentId, String partType, String Description, String productLine, String cognizant,
                  String UoM, String BoM_Line, String partNumber, String BoM_Qty, String BoM_Class, String asset,
                  String model, String extDesc, String configType) {
        this.exParentId = new SimpleStringProperty(parentId);
        this.exPartType = new SimpleStringProperty(partType);
        this.exDescription = new SimpleStringProperty(Description);
        this.exProductLine = new SimpleStringProperty(productLine);
        this.exCognizant = new SimpleStringProperty(cognizant);
        this.exUoM = new SimpleStringProperty(UoM);
        this.exBoM_Line = new SimpleStringProperty(BoM_Line);
        this.exPart_Number = new SimpleStringProperty(partNumber);
        this.exBoM_Qty = new SimpleStringProperty(BoM_Qty);
        this.exBoM_Class = new SimpleStringProperty(BoM_Class);
        this.exAsset = new SimpleStringProperty(asset);
        this.exModel = new SimpleStringProperty(model);
        this.exExtDesc = new SimpleStringProperty(extDesc);
        this.exConfigType = new SimpleStringProperty(configType);
    }

    public String getExParentId() {return exParentId.get(); }
    public String getExPartType() {return exPartType.get(); }
    public String getExDescription() {return exDescription.get(); }
    public String getExProductLine() {return exProductLine.get(); }
    public String getExCognizant() {return exCognizant.get(); }
    public String getExUoM() {return exUoM.get(); }
    public String getExBoM_Line() {return exBoM_Line.get(); }
    public String getExPart_Number() {return exPart_Number.get(); }
    public String getExBoM_Qty() {return exBoM_Qty.get(); }
    public String getExBoM_Class() {return exBoM_Class.get();}
    public String getExAsset() {return exAsset.get(); }
    public String getExModel() {return exModel.get(); }
    public String getExExtDesc() {return exExtDesc.get();}
    public String getExConfigType() {return exConfigType.get();}

    public void setExDescription(String Description) {
        exDescription.set(Description);
    }
    public void setExProductLine(String productLine) {
        exProductLine.set(productLine);
    }
    public void setExAsset(String asset) {
        exAsset.set(asset);
    }
    public void setExExtDesc(String extDesc) {
        exExtDesc.set(extDesc);
    }
}
