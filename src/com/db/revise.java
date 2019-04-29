package com.db;

import com.agile.api.ChangeConstants;
import com.agile.api.ItemConstants;
import com.agile.ws.schema.attachment.v1.jaxws.AddFileAttachmentRequestType;
import com.agile.ws.schema.attachment.v1.jaxws.AddFileAttachmentResponseType;
import com.agile.ws.schema.attachment.v1.jaxws.AgileAddFileAttachmentRequest;
import com.agile.ws.schema.attachment.v1.jaxws.AgileAddFileAttachmentRequestType;
import com.agile.ws.schema.business.v1.jaxws.*;
import com.agile.ws.schema.collaboration.v1.jaxws.*;
import com.agile.ws.schema.common.v1.jaxws.*;
import com.agile.ws.schema.table.v1.jaxws.*;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.db.base.*;
import static com.db.exceptions.*;
import static com.db.sopeShared.*;
import static com.db.sopeMain.*;
import static com.db.prelim.*;

public class revise {
    @FXML
    Button buttonQuit;
    @FXML
    public ChoiceBox<String> choiceBox_Man_Name, choiceBox_Product_Team, choiceBox_Product_Line, choiceBox_ChangeAnalyst;
    @FXML
    public Label label_Price_Category, label_Product_Line, label_Latest_Rev;
    @FXML
    public TextField textField_Man_Part_Number, textField_Man_Part_Desc, textField_Part_Number, textField_Customer;
    @FXML
    public TextField textField_LocalCost, textField_Solution_Type, textField_ExchangeRate, textField_Cost;
    @FXML
    public Label label_Revise_Status, label_Total_Cost, label_Description, label_ECO_Notes, label_Product_Category;
    @FXML
    public Label label_Reference_Cost, label_Floor_Price, label_Reference_Price, label_List_Price, label_Maintenance_Units;
    @FXML
    public ListView<String> listView_Codes;

    private Stage mainStage = getPrimaryStage();

    private static String new_USDcost = "0.0", new_LocalCost = "0.0", new_FXRate = "1.0";
    private static String FXRate = "1.0";
    private double ref_cost, list_price, floor_price, total_cost, maint_units;
    private DecimalFormat df = new DecimalFormat("#.00");
    private int priceCatIndex = -1;

    private final static String CONST_TOTAL_COST = ItemConstants.ATT_PAGE_THREE_NUMERIC02.toString();
    private final static String CONST_LIST_PRICE = ItemConstants.ATT_PAGE_THREE_NUMERIC01.toString();
    private final static String CONST_REFERENCE_COST = ItemConstants.ATT_PAGE_THREE_NUMERIC08.toString();
    private final static String CONST_FLOOR_PRICE = ItemConstants.ATT_PAGE_THREE_MONEY10.toString();
    private final static String CONST_MAINT_UNITS = ItemConstants.ATT_PAGE_THREE_NUMERIC10.toString();
    private final static String CONST_REFERENCE_PRICE = ItemConstants.ATT_PAGE_THREE_MONEY09.toString();
    private final static String CONST_PRICE_CATEGORY = ItemConstants.ATT_PAGE_THREE_LIST01.toString();



    public void initialize() {

        agileECO.itemText = null;
        choiceBox_Man_Name.setItems(manNameChoices);
        choiceBox_Product_Team.setItems(productTeams);
        choiceBox_Product_Line.setItems(prodLineChoices);

        choiceBox_ChangeAnalyst.setItems(analysts);
        customCodeList = new ArrayList<>();

        /* Cost Change Listener */

        textField_Cost.textProperty().addListener((ObservableValue<? extends String> observable,
                                                   String oldValue, String newValue) -> {
            new_USDcost = newValue;
            calculate(new_USDcost, new_LocalCost, new_FXRate);
        });

        textField_LocalCost.textProperty().addListener((ObservableValue<? extends String> observable,
                                                        String oldValue, String newValue) -> {
            new_LocalCost = newValue;
            calculate(new_USDcost, new_LocalCost, new_FXRate);

        });

        textField_ExchangeRate.textProperty().addListener((ObservableValue<? extends String> observable,
                                                           String oldValue, String newValue) -> {
            new_FXRate = newValue;
            calculate(new_USDcost, new_LocalCost, new_FXRate);
            FXRate = new_FXRate;
        });
    }

    public void ButtonCreateECOOnAction() {

        String ECO_Extended_Description = "EFFECTIVITY: UPON RELEASE\n\nMATERIAL DISPOSITION: NONE\n\n" +
                "FIELD IMPACT: NO IMPACT\n \n JIRA#: N/A\n \n" +
                "REASON FOR CHANGE: CUSTOM CODE REQUIRED FOR EXECUTION AND QUOTING IN BIGMACHINES\n \n" +
                "AGENCY COMPLIANCE IMPACT: NO IMPACT\n \n" +
                "OTHER IMPACT:\n GOODS BURDEN: 1.8% \n" +
                "LABOR BURDEN: 3.2% \n NON-GOODS BURDEN: 0%\n EXCHANGE RATE per USD: " + FXRate;
        CreateObjectRequestType createObjectRequestType = new CreateObjectRequestType();
        AgileCreateObjectRequest agileCreateObjectRequest = new AgileCreateObjectRequest();
        agileCreateObjectRequest.setClassIdentifier("ECO");
        AgileRowType row = new AgileRowType();

        if (exceptionName == null) {
            TextInputDialog labelDialog = new TextInputDialog();
            labelDialog.setHeaderText("Enter Exception Number :");
            Optional<String> exceptionInput = labelDialog.showAndWait();
            exceptionName = exceptionInput.toString();
        }

        String ECODescription = choiceBox_Product_Team.getSelectionModel().getSelectedItem() +
                ": CUSTOM CODE UPDATE FOR " + textField_Customer.getText() +
                ". QUOTE REVISION FOR " + textField_Solution_Type.getText() +
                " SOLUTION. AGILE OPPY ID:" + agileId + " SFDC EXCEPTION: " +
                exceptionName;
        if ((agileECO.itemText = sopeShared.getNextAutoNumber("ECO")) != null) {

            Element el_number = sopeShared.createTextElement("number", agileECO.itemText);
            Element el_prodLine = sopeShared.createListElement("productLineS", choiceBox_Product_Line);
            Element el_ECO_desc = sopeShared.createTextElement("descriptionOfChange", ECODescription);
            Element el_Reason = sopeShared.createTextElement("reasonForChange", ECO_Extended_Description);
            Element el_Engineer = sopeShared.createTextElement("text02", agileCognizant);
            Element el_ReasonCode = sopeShared.createListElement("reasonCode", "Cost Reduction",
                    ChangeConstants.ATT_COVER_PAGE_REASON_CODE.toString());
            Element el_Analyst = sopeShared.createListElement("changeAnalyst", choiceBox_ChangeAnalyst);

            row.getAny().add(el_number);
            row.getAny().add(el_prodLine);
            row.getAny().add(el_ECO_desc);
            row.getAny().add(el_Reason);
            row.getAny().add(el_Engineer);
            row.getAny().add(el_ReasonCode);
            row.getAny().add(el_Analyst);
            agileCreateObjectRequest.setData(row);

            createObjectRequestType.getRequests().add(agileCreateObjectRequest);
            CreateObjectResponseType createObjectResponseType = agileBusinessObjectStub.createObject(createObjectRequestType);

            if (createObjectResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
                AgileCreateObjectResponse createObjectResponse = createObjectResponseType.getResponses().get(0);
                agileECO.objectNumber = createObjectResponse.getAgileObject().getObjectIdentifier().getObjectId().toString();
                label_Revise_Status.setText("ECO " + agileECO.itemText + " Create SUCCESS");
                label_Revise_Status.getStyleClass().add("label-success");

            } else {
                label_Revise_Status.setText("ECO Create FAILURE - "
                        + createObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
                label_Revise_Status.getStyleClass().add("label-failure");
                agileECO.itemText = null;
            }
        }
    }

    public void ButtonSelectCustomCodeOnAction() {
        customCode.itemText = textField_Part_Number.getText();
        customCode.objectNumber = agileSearchCode("CustomCode", customCode.itemText, label_Revise_Status);

        GetObjectRequestType getObjectRequestType = new GetObjectRequestType();
        AgileGetObjectRequest agileGetObjectRequest = new AgileGetObjectRequest();
        agileGetObjectRequest.setClassIdentifier("CustomCode");
        agileGetObjectRequest.setObjectNumber(customCode.objectNumber);


        /* Display current Page Three Data */

        AgileDataTableRequestType tableRequests = new AgileDataTableRequestType();
        tableRequests.setTableIdentifier(ItemConstants.TABLE_PAGETHREE.toString());
        tableRequests.setLoadCellMetaData(false);
        agileGetObjectRequest.getTableRequests().add(tableRequests);
        getObjectRequestType.getRequests().add(agileGetObjectRequest);

        GetObjectResponseType getObjectResponseType = agileBusinessObjectStub.getObject(getObjectRequestType);

        if (getObjectResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_Revise_Status.setText("CustomCode" + " - " + customCode.itemText + " getObject  SUCCESS");
            label_Revise_Status.getStyleClass().add("label-success");

            AgileGetObjectResponse response = getObjectResponseType.getResponses().get(0);
            AgileObjectType agileObject = response.getAgileObject();
            List<Element> messages = agileObject.getAny();

            for (Element el : messages) {
                String attributeId = el.getAttribute("attributeId");
                String tagName = el.getTagName();

                if (tagName.equals("description")) {
                    label_Description.setText(getMessageElementValue(el));
                } else if (tagName.equals("productLineS")) {
                    label_Product_Line.setText(getMessageElementValue(el));
                }  else if (tagName.equals("rev")) {
                    label_Latest_Rev.setText(getMessageElementValue(el));
                }

                if (attributeId.equals(CONST_TOTAL_COST))
                    label_Total_Cost.setText(getMessageElementValue(el));
                else if (attributeId.equals(CONST_LIST_PRICE))
                    label_List_Price.setText(getMessageElementValue(el));
                else if (attributeId.equals(CONST_REFERENCE_COST))
                    label_Reference_Cost.setText(getMessageElementValue(el));
                else if (attributeId.equals(CONST_FLOOR_PRICE))
                    label_Floor_Price.setText(getMessageElementValue(el));
                else if (attributeId.equals(CONST_MAINT_UNITS))
                    label_Maintenance_Units.setText(getMessageElementValue(el));
                else if (attributeId.equals(CONST_REFERENCE_PRICE))
                    label_Reference_Price.setText(getMessageElementValue(el));
                else if (attributeId.equals(CONST_PRICE_CATEGORY)) {
                    label_Price_Category.setText(getMessageElementValue(el));
                    priceCatIndex = priceCatChoices.indexOf(getMessageElementValue(el));
                }
            }

            /* Get current Manufacturing Part */

            redlineManPart = getManufacturerPart(customCode.objectNumber,label_Revise_Status);
            if (redlineManPart != null)
                textField_Man_Part_Number.setText(redlineManPart.itemText);

        } else {
            label_Revise_Status.setText("CustomCode" + " - " + customCode.itemText + " getObject  FAILURE");
            label_Revise_Status.getStyleClass().add("label-failure");
        }
    }

    public void ButtonAddManPartOnAction() {
        manPart.itemText = textField_Man_Part_Number.getText();
        agileItem item = agileSearchPart(manPart.itemText, label_Revise_Status);
        manPart.objectNumber = item.objectNumber;
        choiceBox_Man_Name.setValue(item.itemText);

        addManPart();
    }

    /* Create Manufacturer Part + add Quote */

    public void ButtonCreateManPartOnAction() {

        CreateObjectRequestType createObjectRequestType = new CreateObjectRequestType();
        AgileCreateObjectRequest agileCreateObjectRequest = new AgileCreateObjectRequest();
        agileCreateObjectRequest.setClassIdentifier("ManufacturerPart");

        Element el_man = sopeShared.createTextElement("manufacturerName", choiceBox_Man_Name);
        Element el_manNumber = sopeShared.createTextElement("manufacturerPartNumber", textField_Man_Part_Number);
        Element el_desc = sopeShared.createTextElement("description", textField_Man_Part_Desc);
        AgileRowType row = new AgileRowType();
        row.getAny().add(el_man);
        row.getAny().add(el_manNumber);
        row.getAny().add(el_desc);
        agileCreateObjectRequest.setData(row);

        createObjectRequestType.getRequests().add(agileCreateObjectRequest);
        CreateObjectResponseType createObjectResponseType = agileBusinessObjectStub.createObject(createObjectRequestType);
        AgileCreateObjectResponse createObjectResponse = createObjectResponseType.getResponses().get(0);
        if (createObjectResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            manPart.objectNumber = createObjectResponse.getAgileObject().getObjectIdentifier().getObjectId().toString();
            label_Revise_Status.setText("Manufacturer Part Create SUCCESS");
            label_Revise_Status.getStyleClass().add("label-success");
        } else {
            label_Revise_Status.setText("Manufacturer Part Create Create FAILURE - " +
                    createObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Revise_Status.getStyleClass().add("label-failure");
            return;
        }

        /* Add Attachment to Part */

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("pdf file (*.pdf)", "*.pdf"));
        fileChooser.setTitle("Open pdf file");
        File file = fileChooser.showOpenDialog(mainStage);

        AddFileAttachmentRequestType addFileAttachmentRequestType = new AddFileAttachmentRequestType();
        AgileAddFileAttachmentRequest agileAddFileAttachmentRequest = new AgileAddFileAttachmentRequest();
        agileAddFileAttachmentRequest.setClassIdentifier("ManufacturerPart");
        agileAddFileAttachmentRequest.setObjectNumber(manPart.objectNumber);

        AgileAddFileAttachmentRequestType attachments = new AgileAddFileAttachmentRequestType();
        attachments.setName(file.getName());
        attachments.setDescription("Quote PDF");

        FileDataSource datasource = new FileDataSource(file);
        attachments.setContent(new DataHandler(datasource));

        agileAddFileAttachmentRequest.getAttachments().add(attachments);
        agileAddFileAttachmentRequest.setSingleFolder(true);
        addFileAttachmentRequestType.getRequests().add(agileAddFileAttachmentRequest);
        AddFileAttachmentResponseType addFileAttachmentResponseType = agileAttachmentStub.addFileAttachment(addFileAttachmentRequestType);
        if (addFileAttachmentResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_Revise_Status.setText("Manufacturer Part Attachment SUCCESS");
            label_Revise_Status.getStyleClass().add("label-success");

        } else {
            label_Revise_Status.setText("Manufacturer Part Attachment  FAILURE - " +
                    addFileAttachmentResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Revise_Status.getStyleClass().add("label-failure");
            return;
        }

        addManPart();
    }

        /* Add new Manufacturer Part to Custom Code */

    private void addManPart()  {

        AddRowsRequestType addRowsRequestType = new AddRowsRequestType();
        AgileAddRowsRequest agileAddRowsRequest = new AgileAddRowsRequest();

        RequestTableType table = new RequestTableType();
        table.setClassIdentifier("CustomCode");
        table.setObjectNumber(customCode.objectNumber);
        table.setTableIdentifier(ItemConstants.TABLE_REDLINEMANUFACTURERS.toString());

        ObjectReferentIdType objRefId = new ObjectReferentIdType();
        objRefId.setClassIdentifier("ManufacturerPart");
        objRefId.setObjectIdentifier(manPart.objectNumber);

        AgileRowType row = new AgileRowType();

        PropertyType properties_newPart = new PropertyType();
        properties_newPart.setPropertyName( SchemaConstants.REDLINE_CHANGE.value() );
        properties_newPart.setPropertyValue(agileECO.itemText);
        table.getOptions().add(properties_newPart);

        Element el_man = createTextElement("mfrPartNumber", objRefId);

        row.getAny().add(el_man);
        agileAddRowsRequest.getRow().add(row);
        agileAddRowsRequest.setObjectInfo(table);

        addRowsRequestType.getData().add(agileAddRowsRequest);
        AddRowsResponseType addRowsResponseType = agileTableStub.addRows(addRowsRequestType);
        if (addRowsResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_Revise_Status.setText("Add Manufacturer Part SUCCESS");
            label_Revise_Status.getStyleClass().add("label-success");
        } else {
            label_Revise_Status.setText("Add Manufacturer Part FAILURE - " + manPart.itemText + " " +
                    addRowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Revise_Status.getStyleClass().add("label-failure");
            agileECO.itemText = null;

        }
    }

    public void ButtonAddCustomCodeOnAction() {

        RequestTableType table = new RequestTableType();
        table.setClassIdentifier("ECO");
        table.setObjectNumber(agileECO.objectNumber);
        table.setTableIdentifier("Affected Items");

        /* Add Custom Code to ECO Affected Items */

        AddRowsRequestType addRowsRequestType = new AddRowsRequestType();
        AgileAddRowsRequest agileAddRowsRequest = new AgileAddRowsRequest();
        agileAddRowsRequest.setObjectInfo(table);
        AgileRowType row = new AgileRowType();
        ObjectReferentIdType objRefId = new ObjectReferentIdType();
        objRefId.setClassIdentifier("CustomCode");
        objRefId.setObjectIdentifier(customCode.objectNumber);

        Element el_man = sopeShared.createTextElement("itemNumber", objRefId);

        row.getAny().add(el_man);
        agileAddRowsRequest.getRow().add(row);

        addRowsRequestType.getData().add(agileAddRowsRequest);
        AddRowsResponseType addRowsResponseType = agileTableStub.addRows(addRowsRequestType);
        if (addRowsResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_Revise_Status.setText("Add Affected Item " + customCode.itemText + " SUCCESS");
            label_Revise_Status.getStyleClass().add("label-success");

        } else if (addRowsResponseType.getStatusCode().equals(ResponseStatusCode.WARNING)){
            label_Revise_Status.setText("Add Affected Item " + customCode.itemText +
                    addRowsResponseType.getWarnings().get(0).getWarning().get(0).getMessage());
            label_Revise_Status.getStyleClass().add("label-warning");
            return;
        }
        else  {
            label_Revise_Status.setText("Add Affected Item FAILURE - " + customCode.itemText + " " +
                    addRowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Revise_Status.getStyleClass().add("label-failure");
            return;
        }

        /* Update Custom Code Page Three Data */

        UpdateObjectRequestType updateObjectRequestType = new UpdateObjectRequestType();
        AgileUpdateObjectRequest agileUpdateObjectRequest = new AgileUpdateObjectRequest();
        agileUpdateObjectRequest.setClassIdentifier("CustomCode");
        agileUpdateObjectRequest.setObjectNumber(customCode.objectNumber);

        Element el_list = createTextElement("numeric31", list_price,
                ItemConstants.ATT_PAGE_THREE_NUMERIC01.toString());

        Element el_ref_cost = createTextElement("numeric38", ref_cost,
                ItemConstants.ATT_PAGE_THREE_NUMERIC08.toString());

        AgileMoneyType floor = new AgileMoneyType();
        floor.setAmount(floor_price);
        floor.setCurrency("USD");

        Element el_floor = createTextElement("money30", floor,
                ItemConstants.ATT_PAGE_THREE_MONEY10.toString());

        Element el_ref_price = createTextElement("money39", floor,
                ItemConstants.ATT_PAGE_THREE_MONEY09.toString());

        Element el_main_units = createTextElement("numeric30", maint_units,
                ItemConstants.ATT_PAGE_THREE_NUMERIC10.toString());

        row = new AgileRowType();

        row.getAny().add(el_list);
        row.getAny().add(el_ref_cost);
        row.getAny().add(el_floor);
        row.getAny().add(el_ref_price);
        row.getAny().add(el_main_units);
        agileUpdateObjectRequest.setData(row);

        updateObjectRequestType.getRequests().add(agileUpdateObjectRequest);
        UpdateObjectResponseType updateObjectResponseType = agileBusinessObjectStub.updateObject(updateObjectRequestType);

        if (updateObjectResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_Revise_Status.setText("Custom Code Update SUCCESS");
            label_Revise_Status.getStyleClass().add("label-success");

        } else {
            label_Revise_Status.setText("Custom Code Update FAILURE - "
                    + updateObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Revise_Status.getStyleClass().add("label-failure");
            agileECO.itemText = null;

        }

        /* Set Next Revision */

        UpdateRowsRequestType updateRowsRequestType = new UpdateRowsRequestType();
        AgileUpdateRowsRequest agileUpdateRowsRequest = new AgileUpdateRowsRequest();

        AgileUpdateRow updateRow = new AgileUpdateRow();
        updateRow.setRowId(getRowID("ECO", agileECO.objectNumber,
                ChangeConstants.TABLE_AFFECTEDITEMS.toString(), customCode.itemText, "itemNumber"));
        row = new AgileRowType();

        char currentRev = label_Latest_Rev.getText().charAt(0);
        String newRev = String.valueOf((char) (currentRev + 1));
        Element el_newRev = sopeShared.createTextElement("newRev", newRev,
                ChangeConstants.ATT_AFFECTED_ITEMS_NEW_REV.toString());
        row.getAny().add(el_newRev);
        updateRow.setRow(row);

        agileUpdateRowsRequest.getRow().add(updateRow);
        agileUpdateRowsRequest.setObjectInfo(table);

        updateRowsRequestType.getData().add(agileUpdateRowsRequest);
        UpdateRowsResponseType updateRowsResponseType = agileTableStub.updateRows(updateRowsRequestType);
        if (updateRowsResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_Revise_Status.setText("Add new Revision SUCCESS");
            label_Revise_Status.getStyleClass().add("label-success");

        } else {
            label_Revise_Status.setText("Add Affected Item FAILURE - " +
                    updateRowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Revise_Status.getStyleClass().add("label-failure");

        }

        /* Redline current Manufacturer Part */

        redlineCurrentPart();

        listView_Codes.getItems().add(textField_Part_Number.getText());
        customCodeList.add(customCode);
    }

    private void redlineCurrentPart() {

        /* Remove existing Manufacturer Part to Redline table */

        RemoveRowsRequestType removeRowsRequestType = new RemoveRowsRequestType();
        AgileRemoveRowsRequest agileRemoveRowsRequest = new AgileRemoveRowsRequest();

        RequestTableType table = new RequestTableType();
        table.setClassIdentifier("CustomCode");
        table.setObjectNumber(customCode.objectNumber);
        table.setTableIdentifier(ItemConstants.TABLE_REDLINEMANUFACTURERS.toString());

        ObjectReferentIdType objRefId = new ObjectReferentIdType();
        objRefId.setClassIdentifier("ManufacturerPart");
        objRefId.setObjectIdentifier(redlineManPart.objectNumber);

        AgileRowType row = new AgileRowType();

        PropertyType properties_redline = new PropertyType();
        properties_redline.setPropertyName(SchemaConstants.REDLINE_CHANGE.value());
        properties_redline.setPropertyValue(agileECO.itemText);
        table.getOptions().add(properties_redline);

        Element el_man = createTextElement("mfrPartNumber", objRefId);
        row.getAny().add(el_man);
        agileRemoveRowsRequest.getRowId().add(getRowID("CustomCode", customCode.objectNumber,
                ItemConstants.TABLE_MANUFACTURERS.toString(), redlineManPart.itemText, "mfrPartNumber"));
        agileRemoveRowsRequest.setObjectInfo(table);

        removeRowsRequestType.getRows().add(agileRemoveRowsRequest);
        RemoveRowsResponseType removeRowsResponseType = agileTableStub.removeRows(removeRowsRequestType);
        if (removeRowsResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_Revise_Status.setText("Redline Manufacturer Part SUCCESS");
            label_Revise_Status.getStyleClass().add("label-success");
        } else {
            label_Revise_Status.setText("Redline Manufacturer Part FAILURE - " +
                    removeRowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Revise_Status.getStyleClass().add("label-failure");
            agileECO.itemText = null;
        }
    }

    public void ButtonSubmitECOOnAction() {


            SetWorkFlowRequestType setWorkFlowRequestType = new SetWorkFlowRequestType();
            AgileSetWorkFlowRequestType agileSetWorkFlowRequestType = new AgileSetWorkFlowRequestType();
            agileSetWorkFlowRequestType.setClassIdentifier("ECO");
            agileSetWorkFlowRequestType.setObjectNumber(agileECO.objectNumber);

            String workflow = String.valueOf(getWorkFlow("ECO", agileECO.itemText, label_Revise_Status));
            agileSetWorkFlowRequestType.setWorkFlowIdentifier(workflow);


            setWorkFlowRequestType.getSetWorkFlowRequest().add(agileSetWorkFlowRequestType);
            SetWorkFlowResponseType setWorkflowResponseType = agileCollaborationStub.setWorkFlow(setWorkFlowRequestType);
            if (setWorkflowResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS))
                label_Revise_Status.setText("ECO " + agileECO.itemText + " Setup  SUCCESS");
            else
                label_Revise_Status.setText("ECO " + agileECO.itemText + " Setup  FAILURE - " +
                        setWorkflowResponseType.getExceptions().get(0).getException().get(0).getMessage());

            ChangeStatusRequestType changeStatusRequestType = new ChangeStatusRequestType();
            AgileChangeStatusRequestType agileChangeStatusRequestType = new AgileChangeStatusRequestType();

            agileChangeStatusRequestType.setClassIdentifier("ECO");
            agileChangeStatusRequestType.setObjectNumber(agileECO.itemText);
            agileChangeStatusRequestType.setNewStatusIdentifier("Submitted");
            agileChangeStatusRequestType.setComment("Comments");
            agileChangeStatusRequestType.setNotifyChangeAnalyst(true);
            agileChangeStatusRequestType.setAuditRelease(false);
            agileChangeStatusRequestType.setUrgent(false);
            changeStatusRequestType.setDisableAllWarnings(true);
            changeStatusRequestType.getChangeStatusRequest().add(agileChangeStatusRequestType);
            ChangeStatusResponseType changeStatusResponseType = agileCollaborationStub.changeStatus(changeStatusRequestType);
            if (changeStatusResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
                label_Revise_Status.setText("ECO " + agileECO.itemText + " Submit  SUCCESS");
            } else
                label_Revise_Status.setText("ECO " + agileECO.itemText + " Submit FAILURE - " +
                        setWorkflowResponseType.getExceptions().get(0).getException().get(0).getMessage());
    }


    public void ButtonQuitOnAction() {
        Stage stage = (Stage) buttonQuit.getScene().getWindow();
        stage.close();
    }


    private void calculate(String usd, String local, String fx) {

        double costUSD = Double.parseDouble(usd);
        if ( ! local.equals("0.0")) {
            costUSD = Double.parseDouble(local) / Double.parseDouble(fx);
            textField_Cost.setText(df.format(costUSD));
        }
        label_ECO_Notes.setText("Fx Rate : " + new_FXRate);
        if(priceCatIndex < 0 )
            return;
        total_cost = Math.ceil(100.0 * costUSD * burden[priceCatIndex]) / 100.0;
        label_Total_Cost.setText(df.format(total_cost));

        ref_cost = roundup(total_cost);
        label_Reference_Cost.setText(df.format(ref_cost));

        floor_price = roundup(ref_cost / (1.0 - margin[priceCatIndex]));
        label_Floor_Price.setText(df.format(floor_price));
        label_Reference_Price.setText(df.format(floor_price));

        if (priceCatIndex != 3)
            list_price = roundup(ref_cost / (1.0 - margin[priceCatIndex]) / 0.7d);
        else
            list_price = roundup(ref_cost / (1.0 - margin[priceCatIndex]));
        label_List_Price.setText(df.format(list_price));

        maint_units = ref_cost * maint[priceCatIndex];
        label_Maintenance_Units.setText(df.format(maint_units));

    }
}
