package com.db;

import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.*;
import com.sforce.ws.ConnectionException;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;


import static com.db.base.*;
import static com.db.opportunities.*;
import static com.db.sopeMain.*;


public class export {

    @FXML
    Button buttonQuit, buttonExport;
    @FXML
    TableView<exportDisplay> tableViewExport;
    @FXML
    TableView<assetDisplay> tableViewAsset;
    @FXML
    TableView<modelDisplay> tableViewModel;
    @FXML
    TableColumn<exportDisplay, String> column_Parent_Id, column_Part_Type, column_Description, column_Product_Line;
    @FXML
    TableColumn<exportDisplay, String> column_Cognizant, column_UoM, column_BoM_Line, column_Part_Number, column_BoM_Qty;
    @FXML
    TableColumn<exportDisplay, String> column_BoM_Class, column_Asset, column_Model, column_Config_Type, column_Ext_Desc;
    @FXML
    TableColumn<modelDisplay, String> colModelId, colModelNumber, colModelDescription, colModelExtDesc;
    @FXML
    ToggleGroup toggleProduct;
    @FXML
    RadioButton radioXC, radioStorage, radioCluster;
    @FXML
    Label label_Export_Status;

    @FXML
    TableColumn<assetDisplay, String> colAsset, colProduct, colModel;

    private String product, quoteDescription;
    private static ObservableList<assetDisplay> assetData = FXCollections.observableArrayList();
    private static ObservableList<modelDisplay> modelData = FXCollections.observableArrayList();
    private static ObservableList<exportDisplay> exportData = FXCollections.observableArrayList();

    public void initialize () {

        assetData.clear();
        modelData.clear();
        exportData.clear();
        radioXC.setSelected(true);
        product = "CASCADE";
        colAsset.setCellValueFactory(new PropertyValueFactory<>("asAsset"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("asProduct"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("asModel"));

        colModelId.setCellValueFactory(new PropertyValueFactory<>("modModelId"));
        colModelNumber.setCellValueFactory(new PropertyValueFactory<>("modNumber"));
        colModelDescription.setCellValueFactory(new PropertyValueFactory<>("modDescription"));
        colModelExtDesc.setCellValueFactory(new PropertyValueFactory<>("modExtDesc"));

        column_Parent_Id.setCellValueFactory(new PropertyValueFactory<>("exParentId"));
        column_Part_Type.setCellValueFactory(new PropertyValueFactory<>("exPartType"));
        column_Description.setCellValueFactory(new PropertyValueFactory<>("exDescription"));
        column_Product_Line.setCellValueFactory(new PropertyValueFactory<>("exProductLine"));
        column_Cognizant.setCellValueFactory(new PropertyValueFactory<>("exCognizant"));
        column_UoM.setCellValueFactory(new PropertyValueFactory<>("exUoM"));
        column_BoM_Line.setCellValueFactory(new PropertyValueFactory<>("exBoM_Line"));
        column_Part_Number.setCellValueFactory(new PropertyValueFactory<>("exPart_Number"));
        column_BoM_Qty.setCellValueFactory(new PropertyValueFactory<>("exBoM_Qty"));
        column_BoM_Class.setCellValueFactory(new PropertyValueFactory<>("exBoM_Class"));
        column_Asset.setCellValueFactory(new PropertyValueFactory<>("exAsset"));
        column_Model.setCellValueFactory(new PropertyValueFactory<>("exModel"));
        column_Ext_Desc.setCellValueFactory(new PropertyValueFactory<>("exExtDesc"));
        column_Config_Type.setCellValueFactory(new PropertyValueFactory<>("exConfigType"));

        assetDisplay row = new assetDisplay("Misc");
        assetData.add(row);
        row = new assetDisplay("Pro Services");
        assetData.add(row);
        row = new assetDisplay("Training");
        assetData.add(row);
        row = new assetDisplay("Spares");
        assetData.add(row);

        toggleProduct.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov,
                Toggle old_toggle, Toggle new_toggle) -> {
            if(radioXC.isSelected())
                product = "CASCADE";
            else if (radioStorage.isSelected())
                product = "STORAGE";
            else
                product = "CCS";
        });

        /* Model Selection */

        tableViewModel.getSelectionModel().setCellSelectionEnabled(true);
        ObservableList<TablePosition> selectedCells = tableViewModel.getSelectionModel().getSelectedCells();

        selectedCells.addListener((ListChangeListener<TablePosition>) change -> {
                    TablePosition tablePosition = selectedCells.get(0);
                    modelRowSelect(tablePosition);
                });

        loadExport();
        fillExportTable();
    }

    public void ButtonQuitOnAction () {
        Stage stage = (Stage) buttonQuit.getScene().getWindow();
        stage.close();
    }

    private void modelRowSelect(TablePosition position) {

        String modelName = tableViewModel.getColumns().get(2).getCellObservableValue(position.getRow()).toString();
        if (modelName == null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Export Model");
            alert.setContentText("Please Enter a Model Name");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == ButtonType.OK)
                    return;
            }
        }

        if (tableViewAsset.getSelectionModel().getSelectedItems() == null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Export Asset");
            alert.setContentText("Please Select an Asset before Model Assignment");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == ButtonType.OK)
                    return;
            }
        }
        String modelAsset = tableViewAsset.getSelectionModel().getSelectedItems().get(0).getAsAsset();
        String modelDescription = tableViewModel.getSelectionModel().getSelectedItems().get(0).getModDescription();
        String modelExtDesc = tableViewModel.getSelectionModel().getSelectedItems().get(0).getModExtDesc();
        String modelNum = tableViewModel.getSelectionModel().getSelectedItems().get(0).getModNumber();
        String serialAsset = modelAsset;
        if (tableViewAsset.getSelectionModel().getSelectedIndex() > 3)
            serialAsset = "SN" + modelAsset;
        for(exportDisplay row : tableViewExport.getItems()) {
            if (row.getExModel().equals(modelNum)) {

                row.setExDescription((quoteDescription + " " + modelDescription + " " + serialAsset).toUpperCase());
                row.setExProductLine(product);
                row.setExAsset(modelAsset);
                if (modelExtDesc != null)
                    row.setExExtDesc(modelExtDesc.toUpperCase());
            }
        }
        tableViewExport.refresh();

    }

    public void ButtonExportOnAction () {

        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            DataFormat format = wb.createDataFormat();
            Sheet exportSheet = wb.createSheet();

            HSSFPalette palette = wb.getCustomPalette();

            Font exportFont =  wb.createFont();
            exportFont.setFontName("Calibri");

            CellStyle headerStyle = wb.createCellStyle();
            palette.setColorAtIndex (IndexedColors.BLUE.index, (byte)68, (byte)114,(byte)196);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.index);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = wb.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontName("Calibri");
            headerStyle.setFont(headerFont);

            CellStyle oddRowStyle = wb.createCellStyle();
            palette.setColorAtIndex (IndexedColors.LIGHT_BLUE.index,(byte)217, (byte)225,(byte)242);
            oddRowStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.index);
            oddRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            oddRowStyle.setFont(exportFont);

            CellStyle evenRowStyle = wb.createCellStyle();
            evenRowStyle.setFillForegroundColor(IndexedColors.WHITE.index);
            evenRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            evenRowStyle.setFont(exportFont);

            CellStyle assetStyle = wb.createCellStyle();
            assetStyle.setFillForegroundColor(IndexedColors.CORAL.index);
            assetStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            assetStyle.setFont(exportFont);

            CellStyle justifyOddStyle = wb.createCellStyle();
            justifyOddStyle.setAlignment(HorizontalAlignment.RIGHT);
            justifyOddStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.index);
            justifyOddStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            justifyOddStyle.setDataFormat(format.getFormat("0"));
            justifyOddStyle.setFont(exportFont);

            CellStyle justifyEvenStyle = wb.createCellStyle();
            justifyEvenStyle.setAlignment(HorizontalAlignment.RIGHT);
            justifyEvenStyle.setFillForegroundColor(IndexedColors.WHITE.index);
            justifyEvenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            justifyOddStyle.setDataFormat(format.getFormat("0"));
            justifyEvenStyle.setFont(exportFont);

            Row row = exportSheet.createRow(0);

            for (int j = 0; j < tableViewExport.getColumns().size(); j++) {
                Cell headerCell = row.createCell(j);
                headerCell.setCellStyle(headerStyle);
                headerCell.setCellValue(tableViewExport.getColumns().get(j).getText());
            }

            for (int i = 0; i < tableViewExport.getItems().size(); i++) {
                row = exportSheet.createRow(i + 1);
                for (int j = 0; j < tableViewExport.getColumns().size(); j++) {
                    Cell rowCell = row.createCell(j);
                    if (tableViewExport.getColumns().get(j).getCellData(i) != null)
                        if (j == 6 || j == 8 || j == 11)
                            rowCell.setCellValue(Integer.parseInt(tableViewExport.getColumns().get(j).getCellData(i).toString()));
                        else
                            rowCell.setCellValue(tableViewExport.getColumns().get(j).getCellData(i).toString());
                    else
                        rowCell.setCellValue("");
                    if (i % 2 == 0) {
                        rowCell.setCellStyle(oddRowStyle);
                        if (j == 6 || j == 8 || j == 11)
                            rowCell.setCellStyle(justifyOddStyle);
                    } else {
                        rowCell.setCellStyle(evenRowStyle);
                        if (j == 6 || j == 8 || j == 11)
                            rowCell.setCellStyle(justifyEvenStyle);
                    }
                    if (j == 10)
                        rowCell.setCellStyle(assetStyle);
                }
            }


            DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
            DateTimeFormatter month = DateTimeFormatter.ofPattern("MM");
            DateTimeFormatter day = DateTimeFormatter.ofPattern("dd");
            String fileName = quoteDescription + "-" + year.format(LocalDateTime.now()) + "y" +
                    month.format(LocalDateTime.now()) + "m" + day.format(LocalDateTime.now()) + "d.xls";
            DirectoryChooser dc = new DirectoryChooser();
            File selectedDirectory = dc.showDialog(getPrimaryStage());
            String filePath = selectedDirectory.getAbsolutePath() + "\\" + fileName;

            File exportFile = new File(filePath);
                        if (exportFile.exists())
                if ( !exportFile.delete() )
                    label_Export_Status.setText(fileName + " Deletion Error");
            if (exportFile.createNewFile()) {
                FileOutputStream fileOut = new FileOutputStream(exportFile);
                wb.write(fileOut);
                fileOut.close();

                label_Export_Status.setText(fileName + " Export Complete");
                label_Export_Status.getStyleClass().add("label-success");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void loadExport() {

        tableViewAsset.setItems(assetData);
        tableViewModel.setItems(modelData);
        tableViewExport.setItems(exportData);

        String soqlQuery = "SELECT Quote_Model_Information__c, Asset_Transaction__c, Transaction_Product__c FROM Opportunity_Transaction__c WHERE Quote_Number_Quote__c = '" +
                QuoteNumber + "' AND Asset_Transaction__c <> Null";
        try {
            QueryResult qr = connection.query(soqlQuery);
            SObject[] exRecords = qr.getRecords();
            if (exRecords != null && exRecords.length> 0) {
                StringBuilder outerQuery = new StringBuilder("SELECT Name FROM Asset WHERE Id in ('");
                Opportunity_Transaction__c[] ot = new Opportunity_Transaction__c[exRecords.length];
                for (int i = 0; i < exRecords.length; i++)
                {
                    ot[i] = (Opportunity_Transaction__c)exRecords[i];
                    if (i == 0)
                        outerQuery.append(ot[0].getAsset_Transaction__c());
                    else {
                        outerQuery.append("', '");
                        outerQuery.append(ot[i].getAsset_Transaction__c());
                    }
                }
                outerQuery.append("')");
                QueryResult iq = connection.query(outerQuery.toString());
                exRecords = iq.getRecords();
                if (exRecords != null) {
                    Asset[] aqp = new Asset[exRecords.length];
                    for (int i = 0; i < exRecords.length; i++) {
                        String quoteModel = ot[i].getQuote_Model_Information__c().substring(2, 5);
                        aqp[i] = (Asset) exRecords[i];
                        assetDisplay row = new assetDisplay(aqp[i].getName(), ot[i].getTransaction_Product__c(), quoteModel);
                        assetData.add(row);
                    }
                }
            }
        }
        catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }
    private void fillExportTable() {

        try {
            String soqlQuery = "SELECT Account_Abbr_Name_Opportunity__c, Agile_Opportunity_ID_Opportunity__c, Opportunity_Name_Quote__c FROM " +
                    "Opportunity_Transaction__c WHERE  Quote_Number_Quote__c = '" + QuoteNumber + "' LIMIT 1";
            QueryResult qr = connection.query(soqlQuery);
            SObject[] exRecords = qr.getRecords();
            if (exRecords.length == 0) {
                label_Export_Status.setText("Opportunity Transactions not available");
                return;
            }
            Opportunity_Transaction__c oth = (Opportunity_Transaction__c)exRecords[0];

            soqlQuery = "SELECT Revision__c, Version__c FROM BigMachines__Quote__c WHERE BigMachines__Opportunity__c ='" + QuoteId + "' AND BigMachines__Is_Primary__c = true";
            qr = connection.query(soqlQuery);
            exRecords = qr.getRecords();
            BigMachines__Quote__c bmp = (BigMachines__Quote__c)exRecords[0];

            String quoteVersion = "V" + String.format("%03d", (int)bmp.getRevision__c());
            quoteDescription = (oth.getAccount_Abbr_Name_Opportunity__c().replace("/", "-") + " " + oth.getOpportunity_Name_Quote__c()
                    + " Q" + QuoteNumber + quoteVersion);

            soqlQuery = "SELECT BigMachines_BOM_Class__c, BigMachines__Quantity__c, Item_Line_Number__c, Model__c, Name, Parent_Doc__c  " +
                "FROM BigMachines__Quote_Product__c WHERE Quote_Number__c = '" + QuoteNumber + "' ORDER BY Parent_Doc__c";

            qr = connection.query(soqlQuery);
            if (qr.getSize() > 0) {
                exRecords = qr.getRecords();
                int exportRows = exRecords.length;
                BigMachines__Quote_Product__c[] eqp = new BigMachines__Quote_Product__c[exportRows];

                StringBuilder parentDoc = new StringBuilder("('");
                HashMap<String, Integer> map = new HashMap<>();
                int lineBoM = 0;
                for(int i = 0; i < exportRows; i++) {
                    eqp[i] = (BigMachines__Quote_Product__c)exRecords[i];
                    exportDisplay row = new exportDisplay((oth.getAgile_Opportunity_ID_Opportunity__c() + "-" + eqp[i].getParent_Doc__c()).toUpperCase(),
                            "SYSTEM BOM",
                            quoteDescription,
                            "",
                            agileCognizant.toUpperCase(),
                            "EA",
                            Integer.toString(++lineBoM),
                            (eqp[i].getName()).toUpperCase(),
                            Integer.toString((int) eqp[i].getBigMachines__Quantity__c()),
                            (eqp[i].getBigMachines_BOM_Class__c()).toUpperCase(),
                            "",
                            (eqp[i].getParent_Doc__c()).toUpperCase(),
                            "",
                            (eqp[i].getModel__c()).toUpperCase() );
                    exportData.add(row);

                    String key = eqp[i].getParent_Doc__c();
                    if (i == 0) {
                        parentDoc.append(opportunity[rowSelect].getQualified_Quote__c());
                        parentDoc.append("-");
                        parentDoc.append(String.format("%04d",  Integer.parseInt(key)));
                        map.put(key, i);
                    }
                    else {
                        if (!map.containsKey(key))
                        {
                            parentDoc.append("', '");
                            parentDoc.append(opportunity[rowSelect].getQualified_Quote__c());
                            parentDoc.append("-");
                            parentDoc.append(String.format("%04d",  Integer.parseInt(key)));
                            map.put(key, i);
                        }
                    }
                }
                parentDoc.append("')");

                soqlQuery = "SELECT Doc_Config_Name__c, Doc_Description__c, Doc_Number__c, Id, Model_Name__c FROM CPQ_Model__c WHERE Model_Name__c IN "
                        + parentDoc.toString();

                qr = connection.query(soqlQuery);
                SObject[] modelRecords = qr.getRecords();
                CPQ_Model__c[] model = new CPQ_Model__c[modelRecords.length];

                for (int i = 0; i < modelRecords.length; i++) {
                    model[i] = (CPQ_Model__c) modelRecords[i];
                    modelDisplay row = new modelDisplay(Integer.toString(i + 1), model[i].getDoc_Number__c(), model[i].getDoc_Config_Name__c(),
                            model[i].getDoc_Description__c());

                    modelData.add(row);
                }
            }
        } catch (ConnectionException ce) {
                ce.printStackTrace();
        }

    }

}
