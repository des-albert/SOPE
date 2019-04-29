package com.db;

import javafx.beans.property.SimpleStringProperty;

public class assetDisplay {
    private final SimpleStringProperty asAsset;
    private final SimpleStringProperty asProduct;
    private final SimpleStringProperty asModel;

    assetDisplay(String asset) {
        this.asAsset = new SimpleStringProperty(asset);
        this.asProduct = new SimpleStringProperty("");
        this.asModel = new SimpleStringProperty("");
    }

    assetDisplay(String asset, String product, String model) {

        this.asAsset = new SimpleStringProperty(asset);
        this.asProduct = new SimpleStringProperty(product);
        this.asModel= new SimpleStringProperty(model);
    }
    public String getAsAsset () {return asAsset.get();}
    public String getAsProduct () {return asProduct.get();}
    public String getAsModel () {return asModel.get();}

}
