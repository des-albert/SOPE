package com.db;

import com.agile.api.ChangeConstants;
import com.agile.api.ItemConstants;
import com.agile.ws.schema.business.v1.jaxws.*;
import com.agile.ws.schema.collaboration.v1.jaxws.*;
import com.agile.ws.schema.common.v1.jaxws.*;
import com.agile.ws.schema.table.v1.jaxws.*;
import com.sun.jna.platform.win32.LowLevelMonitorConfigurationAPI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static com.db.base.*;
import static com.db.sopeShared.*;
import static com.db.exceptions.*;

public class firstArticle {

    @FXML
    public ChoiceBox<String>  choiceBox_Product_Line, choiceBox_ChangeAnalyst, choiceBox_Product_Team;
    @FXML
    Button buttonQuit, buttonCreateMCO, buttonGetCodes;
    @FXML
    TextField textField_Customer, textField_Solution_Type;
    @FXML
    ListView<String> listView_MCO_Codes;
    @FXML
    Label label_FA_Status;
    @FXML
    CheckBox checkBox_SalesException;

    ArrayList<agileItem> ecoCodeList;
    String lifeCycle;

    public void initialize ()

    {
        choiceBox_ChangeAnalyst.setItems(analysts);
        choiceBox_Product_Team.setItems(productTeams);
        choiceBox_Product_Line.setItems(prodLineChoices);
        agileMCO.itemText = null;

    }
    public void ButtonGetCodesOnAction() {
        ecoCodeList = new ArrayList<>();
        String ecoObjectNumber = agileSearchChange("ECO", agileECO.itemText, label_FA_Status);
        if (ecoObjectNumber == null) {
            label_FA_Status.setText("ECO " + agileECO.itemText + " not found");
            label_FA_Status.getStyleClass().add("label-failure");
            return;
        }
        ecoCodeList = agileAffectedItems(ecoObjectNumber, label_FA_Status);


        ObservableList<String> mcoCodes = FXCollections.observableArrayList();
        for (agileItem item : ecoCodeList)
            mcoCodes.add(item.itemText);
        listView_MCO_Codes.setItems(mcoCodes);

        /* Check current LifeCycle Status */

        customCode = ecoCodeList.get(0);
        GetObjectRequestType getObjectRequestType = new GetObjectRequestType();
        AgileGetObjectRequest agileGetObjectRequest = new AgileGetObjectRequest();
        agileGetObjectRequest.setClassIdentifier("CustomCode");
        agileGetObjectRequest.setObjectNumber(customCode.objectNumber);

        AgileDataTableRequestType tableRequests = new AgileDataTableRequestType();
        tableRequests.setTableIdentifier(ItemConstants.TABLE_ATTACHMENTS.toString());
        tableRequests.setLoadCellMetaData(false);
        agileGetObjectRequest.getTableRequests().add(tableRequests);

        getObjectRequestType.getRequests().add(agileGetObjectRequest);

        GetObjectResponseType getObjectResponseType = agileBusinessObjectStub.getObject(getObjectRequestType);
        if (getObjectResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_FA_Status.setText("Get Object " + customCode.itemText + " SUCCESS");
            label_FA_Status.getStyleClass().add("label-success");
        } else {
            label_FA_Status.setText("Get Object " + customCode.itemText + " FAILURE - " +
                    getObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_FA_Status.getStyleClass().add("label-failure");
            return;

        }
        AgileObjectType agileObject = getObjectResponseType.getResponses().get(0).getAgileObject();
        List<Element> messages = agileObject.getAny();

        lifeCycle = null;
        for (Element element : messages) {
            if(element.getTagName().equals("lifecyclePhase"))
                    lifeCycle = element.getTextContent();
        }
    }

    public void ButtonCreateMCOOnAction() {
        if (! lifeCycle.equals("FA")) {
            if (createMCO())
                if (attachAffectedItems())
                    submitMCO();
        }
        else
            agileMCO.itemText = agileECO.itemText;
    }

    private boolean createMCO() {

        String MCODescription, MCOExtendedDescription;

        if (checkBox_SalesException.isSelected()) {
            MCOExtendedDescription = "EFFECTIVITY: UPON RELEASE\n\nMATERIAL DISPOSITION: NONE\n\n" +
                "FIELD IMPACT: NO IMPACT\n \n " +
                "REASON FOR CHANGE: REQUEST FOR THIS ITEM HAS BEEN APPROVED\n" +
                "BY SALES MANAGEMENT AND CAN BE RELEASED TO FA FOR QUOTING PURPOSES.\n \n" +
                "OTHER IMPACT: NONE\n";
            MCODescription = choiceBox_Product_Team.getSelectionModel().getSelectedItem() +
                ": SALES LEAD EXCEPTION - CUSTOM CODES FOR " + textField_Customer.getText() +
                " ADVANCE LIFECYCLE OF  " + textField_Solution_Type.getText() + " SOLUTION TO FA. \n" +
                " THESE CUSTOM CODES WERE PREVIOUSLY RELEASED TO PRELIMINARY AND HAVE SINCE BEEN APPROVED BY" +
                " SALES MANAGEMENT - SFDC EXCEPTION " + exceptionName;
        }

        else {
            MCOExtendedDescription = "EFFECTIVITY: UPON RELEASE\n\nMATERIAL DISPOSITION: NONE\n\n" +
                "FIELD IMPACT: NO IMPACT\n \n " +
                "BY THE PRODUCT TEAM AND CAN BE RELEASED TO FA FOR QUOTING PURPOSES.\n \n" +
                "OTHER IMPACT: NONE\n";
            MCODescription = choiceBox_Product_Team.getSelectionModel().getSelectedItem() +
                ": CUSTOM CODES FOR " + textField_Customer.getText() +
                " ADVANCE LIFECYCLE OF  " + textField_Solution_Type.getText() + " SOLUTION TO FA. \n" +
                " THESE CUSTOM CODES WERE PREVIOUSLY RELEASED TO PRELIMINARY AND HAVE SINCE BEEN APPROVED " +
                " BY THE PRODUCT TEAM - SFDC EXCEPTION " + exceptionName;
        }

        CreateObjectRequestType createObjectRequestType = new CreateObjectRequestType();
        AgileCreateObjectRequest agileCreateObjectRequest = new AgileCreateObjectRequest();
        agileCreateObjectRequest.setClassIdentifier("MCO");
        AgileRowType row = new AgileRowType();


        if ((agileMCO.itemText = getNextAutoNumber("MCO")) != null) {

             Element el_number = createTextElement("number", agileMCO.itemText);
             Element el_prodLine = createListElement("productLineS", choiceBox_Product_Line);
             Element el_ECO_desc = createTextElement("descriptionOfChange", MCODescription);
             Element el_Reason = createTextElement("reasonForChange", MCOExtendedDescription);
             Element el_Engineer = createTextElement("text02", agileCognizant);
             Element el_ReasonCode = createListElement("reasonCode", "Release",
                     ChangeConstants.ATT_COVER_PAGE_REASON_CODE.toString());
             Element el_Analyst = createListElement("componentEngineer", choiceBox_ChangeAnalyst);

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
                agileMCO.objectNumber = createObjectResponse.getAgileObject().getObjectIdentifier().getObjectId().toString();
                label_FA_Status.setText("MCO " + agileMCO.itemText + " Create SUCCESS");
                label_FA_Status.getStyleClass().add("label-success");
                return true;
            } else {
                label_FA_Status.setText("MCO Create FAILURE - "
                        + createObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
                label_FA_Status.getStyleClass().add("label-failure");
                return false;
            }

        }
        return false;
    }

    private boolean attachAffectedItems() {

        RequestTableType table = new RequestTableType();
        table.setClassIdentifier("MCO");
        table.setObjectNumber(agileMCO.objectNumber);
        table.setTableIdentifier("Affected Items");

        for(agileItem code : ecoCodeList) {
            AddRowsRequestType addRowsRequestType = new AddRowsRequestType();
            AgileAddRowsRequest agileAddRowsRequest = new AgileAddRowsRequest();
            agileAddRowsRequest.setObjectInfo(table);
            AgileRowType row = new AgileRowType();
            ObjectReferentIdType objRefId = new ObjectReferentIdType();
            objRefId.setClassIdentifier("CustomCode");
            objRefId.setObjectIdentifier(code.objectNumber);

            Element el_man = createTextElement("itemNumber", objRefId);

            row.getAny().add(el_man);
            agileAddRowsRequest.getRow().add(row);

            addRowsRequestType.getData().add(agileAddRowsRequest);

            /*  Ignore Warnings when Code is already attached to a change
            addRowsRequestType.setDisableAllWarnings(true); */

            AddRowsResponseType addRowsResponseType = agileTableStub.addRows(addRowsRequestType);
            if (addRowsResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
                label_FA_Status.setText("MCO Add Affected Item " + code.itemText + " SUCCESS");
                label_FA_Status.getStyleClass().add("label-success");

            }
            else if (addRowsResponseType.getStatusCode().equals(ResponseStatusCode.WARNING)) {
                label_FA_Status.setText("MCO Add Affected Item WARNING - " + code.itemText + " " +
                        addRowsResponseType.getWarnings().get(0).getWarning().get(0).getMessage());
                label_FA_Status.getStyleClass().add("label-warning");
            }

            else {
                label_FA_Status.setText("MCO Add Affected Item FAILURE - " + code.itemText + " " +
                        addRowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
                label_FA_Status.getStyleClass().add("label-failure");
                agileMCO.itemText = null;
                return false;
            }

            /* Set LifeCycle Phase */

            UpdateRowsRequestType updateRowsRequestType = new UpdateRowsRequestType();
            AgileUpdateRowsRequest agileUpdateRowsRequest = new AgileUpdateRowsRequest();

            AgileUpdateRow updateRow = new AgileUpdateRow();
            updateRow.setRowId(getRowID("MCO", agileMCO.objectNumber,
                    ChangeConstants.TABLE_AFFECTEDITEMS.toString(), code.itemText, "itemNumber" ));
            row = new AgileRowType();

            Element el_lifeCycle = createListElement("lifecyclePhase", "FA",
                    ChangeConstants.ATT_AFFECTED_ITEMS_LIFECYCLE_PHASE.toString());
            row.getAny().add(el_lifeCycle);
            updateRow.setRow(row);

            agileUpdateRowsRequest.getRow().add(updateRow);
            agileUpdateRowsRequest.setObjectInfo(table);

            updateRowsRequestType.getData().add(agileUpdateRowsRequest);
            UpdateRowsResponseType updateRowsResponseType = agileTableStub.updateRows(updateRowsRequestType);
            if (updateRowsResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
                label_FA_Status.setText("Change Lifecycle" + code.itemText + " to FA SUCCESS");
                label_FA_Status.getStyleClass().add("label-success");

            }
            else {
                label_FA_Status.setText("Add Affected Item FAILURE - " + code.itemText + " " +
                        updateRowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
                label_FA_Status.getStyleClass().add("label-failure");
                agileMCO.itemText = null;
                return false;
            }
        }
        return true;
    }

    private void submitMCO() {

        SetWorkFlowRequestType setWorkFlowRequestType = new SetWorkFlowRequestType();
        AgileSetWorkFlowRequestType agileSetWorkFlowRequestType =  new AgileSetWorkFlowRequestType();
        agileSetWorkFlowRequestType.setClassIdentifier("MCO");
        agileSetWorkFlowRequestType.setObjectNumber( agileMCO.objectNumber );

        String workflow = String.valueOf(getWorkFlow("MCO", agileMCO.itemText, label_FA_Status));
        agileSetWorkFlowRequestType.setWorkFlowIdentifier( workflow );


        setWorkFlowRequestType.getSetWorkFlowRequest().add(agileSetWorkFlowRequestType);
        SetWorkFlowResponseType setWorkflowResponseType = agileCollaborationStub.setWorkFlow(setWorkFlowRequestType);
        if (setWorkflowResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_FA_Status.setText("MCO " + agileMCO.itemText + " Workflow  SUCCESS");
            label_FA_Status.getStyleClass().add("label-success");
        }
        else{
            label_FA_Status.setText("MCO " + agileMCO.itemText + " Workflow  FAILURE - " +
                    setWorkflowResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_FA_Status.getStyleClass().add("label-failure");
            agileMCO.itemText = null;
        }

        ChangeStatusRequestType changeStatusRequestType = new ChangeStatusRequestType();
        AgileChangeStatusRequestType agileChangeStatusRequestType = new AgileChangeStatusRequestType();

        agileChangeStatusRequestType.setClassIdentifier("MCO");
        agileChangeStatusRequestType.setObjectNumber( agileMCO.itemText );
        agileChangeStatusRequestType.setNewStatusIdentifier( "Submitted");
        agileChangeStatusRequestType.setComment("Comments");
        agileChangeStatusRequestType.setNotifyChangeAnalyst(true);
        agileChangeStatusRequestType.setAuditRelease(false);
        agileChangeStatusRequestType.setUrgent(false);


        changeStatusRequestType.getChangeStatusRequest().add(agileChangeStatusRequestType);
        ChangeStatusResponseType changeStatusResponseType = agileCollaborationStub.changeStatus(changeStatusRequestType);
        if (changeStatusResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_FA_Status.setText("MCO " + agileMCO.itemText + " Submit  SUCCESS");
            label_FA_Status.getStyleClass().add("label-success");
        }
        else {
            label_FA_Status.setText("MCO " + agileMCO.itemText + " Submit FAILURE - " +
                    setWorkflowResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_FA_Status.getStyleClass().add("label-failure");
            agileMCO.itemText = null;
        }
    }


    public void ButtonQuitOnAction () {
        Stage stage = (Stage) buttonQuit.getScene().getWindow();
        stage.close();
    }
}
