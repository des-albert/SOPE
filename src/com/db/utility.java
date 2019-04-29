package com.db;

import com.agile.api.ChangeConstants;
import com.agile.api.ItemConstants;
import com.agile.ws.schema.business.v1.jaxws.*;
import com.agile.ws.schema.collaboration.v1.jaxws.*;
import com.agile.ws.schema.common.v1.jaxws.*;
import com.agile.ws.schema.table.v1.jaxws.*;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.soap.enterprise.sobject.Task;
import com.sforce.ws.ConnectionException;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.w3c.dom.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.db.base.*;
import static com.db.exceptions.*;
import static com.db.sopeShared.*;



public class utility {
    @FXML
    Button button_Quit, button_Code_SD;
    @FXML
    TextField textField_Code, textField_Solution_Type, textField_AgileId, textField_ExceptionName;
    @FXML
    Label label_Utility_Status, label_Description, label_LifeCycle, label_product_Line, label_Total_Cost;
    @FXML
    ChoiceBox<String> choiceBox_Product_Team, choiceBox_ChangeAnalyst, choiceBox_Product_Line;
    @FXML
    TextArea textAreaApprovals;
    @FXML
    ListView<String> listView_Codes;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public void initialize() {
        
        choiceBox_ChangeAnalyst.setItems(analysts);
        choiceBox_Product_Team.setItems(productTeams);
        choiceBox_Product_Line.setItems(prodLineChoices);

        agileMCO.itemText = null;

        customCodeList = new ArrayList<>();
    }

    public void ButtonGetDataOnAction()
    {
        customCode.itemText = textField_Code.getText();
        String partCode;
        if(customCode.itemText.substring(1,2).equals("CE"))
            partCode = "CustomCode";
        else
            partCode = "Parts";
        customCode.objectNumber = agileSearchCode(partCode, customCode.itemText, label_Utility_Status);
        if (customCode.objectNumber == null) {
            return;
        }

        GetObjectRequestType getObjectRequestType = new GetObjectRequestType();
        AgileGetObjectRequest agileGetObjectRequest = new AgileGetObjectRequest();
        agileGetObjectRequest.setClassIdentifier(partCode);
        agileGetObjectRequest.setObjectNumber(customCode.objectNumber);

        AgileDataTableRequestType tableRequests = new AgileDataTableRequestType();
        tableRequests.setTableIdentifier( ItemConstants.TABLE_ATTACHMENTS.toString() );
        tableRequests.setLoadCellMetaData(false);
        agileGetObjectRequest.getTableRequests().add(tableRequests);

        getObjectRequestType.getRequests().add(agileGetObjectRequest);

        GetObjectResponseType getObjectResponseType = agileBusinessObjectStub.getObject(getObjectRequestType);
        if (getObjectResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS))
        {
            label_Utility_Status.setText("Get Object " + customCode.itemText + " SUCCESS");
            label_Utility_Status.getStyleClass().add("label-success");
        } else {
            label_Utility_Status.setText("Get Object " + customCode.itemText + " FAILURE - " +
                    getObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Utility_Status.getStyleClass().add("label-failure");

        }
        AgileObjectType agileObject = getObjectResponseType.getResponses().get(0).getAgileObject();
        List<Element> messages =  agileObject.getAny();

        SelectionType selection;
        for(Element element: messages) {
            switch (element.getTagName()) {
                case "description" :
                    label_Description.setText(element.getTextContent());
                    break;
                case "lifecyclePhase" :
                    AgileListEntryType list = (AgileListEntryType) unmarshalToAgileListEntryType(element);
                    if (list != null ) {
                        if (list.getSelection().size() != 0) {
                            selection = list.getSelection().get(0);
                            label_LifeCycle.setText(selection.getValue());
                        }
                    }
                    break;
                case "productLineS" :
                    list = (AgileListEntryType) unmarshalToAgileListEntryType(element);
                    if (list != null) {
                        if (list.getSelection().size() != 0) {
                            selection = list.getSelection().get(0);
                            label_product_Line.setText(selection.getValue());
                        }
                    }
                    break;
                case "numeric02"  :
                    label_Total_Cost.setText(element.getTextContent());
            }
        }

    }

    public void ButtonPrelimCodeOnAction() {

        agileId = textField_AgileId.getText();
        exceptionName = textField_ExceptionName.getText();
        try {
            FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("prelim.fxml"));
            Parent prelimForm = fxmlFormLoader.load();
            Stage prelimStage = new Stage();
            prelimStage.setTitle("New Custom Code");
            prelimStage.setScene(new Scene(prelimForm));
            prelimStage.show();

        }
        catch (IOException ex) {
            label_Utility_Status.setText("preliminary FXML Loader Exception");
        }
    }

    public void ButtonAddCodeOnAction() {
        listView_Codes.getItems().add(customCode.itemText);
        agileItem newCode = new agileItem(customCode.itemText, customCode.objectNumber);
        customCodeList.add(newCode);
    }
    public void ButtonCreateMCOOnAction() {
        if (createMCO())
            if(attachAffectedItems())
                submitMCO();
    }

    private boolean createMCO() {

        String MCOExtendedDescription = "EFFECTIVITY: UPON RELEASE\n\nMATERIAL DISPOSITION: NONE\n\n" +
                "FIELD IMPACT: NO IMPACT\n \n " +
                "REASON FOR CHANGE: CUSTOM CODE NOT REQUIRED\n" +
                            "OTHER IMPACT: NONE\n";
        CreateObjectRequestType createObjectRequestType = new CreateObjectRequestType();
        AgileCreateObjectRequest agileCreateObjectRequest = new AgileCreateObjectRequest();
        agileCreateObjectRequest.setClassIdentifier("MCO");
        AgileRowType row = new AgileRowType();

        String MCODescription = choiceBox_Product_Team.getSelectionModel().getSelectedItem() +
                " CHANGE LIFECYCLE OF  " + textField_Solution_Type.getText() + " SOLUTION TO SD. \n" +
                " THESE CUSTOM CODE WAS PREVIOUSLY RELEASED AS PRELIMINARY OR FA ";

        if ((agileMCO.itemText = getNextAutoNumber("MCO")) != null) {

            Element el_number = createTextElement("number", agileMCO.itemText);
            Element el_prodLine = createListElement("productLineS", choiceBox_Product_Line);
            Element el_ECO_desc = createTextElement("descriptionOfChange", MCODescription);
            Element el_Reason = createTextElement("reasonForChange", MCOExtendedDescription);
            Element el_Engineer = createTextElement("text02", agileCognizant);
            Element el_ReasonCode = createListElement("reasonCode", "Design Change",
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
                label_Utility_Status.setText("MCO " + agileMCO.itemText + " Create SUCCESS");
                label_Utility_Status.getStyleClass().add("label-success");
                return true;
            } else {
                label_Utility_Status.setText("MCO" + agileMCO.itemText + "Create FAILURE - "
                        + createObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
                label_Utility_Status.getStyleClass().add("label-failure");
                agileMCO.itemText = null;
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

        for(agileItem item : customCodeList) {
            AddRowsRequestType addRowsRequestType = new AddRowsRequestType();
            AgileAddRowsRequest agileAddRowsRequest = new AgileAddRowsRequest();
            agileAddRowsRequest.setObjectInfo(table);
            AgileRowType row = new AgileRowType();
            ObjectReferentIdType objRefId = new ObjectReferentIdType();
            objRefId.setClassIdentifier("CustomCode");
            objRefId.setObjectIdentifier(item.objectNumber);

            Element el_man = createTextElement("itemNumber", objRefId);

            row.getAny().add(el_man);
            agileAddRowsRequest.getRow().add(row);

            addRowsRequestType.getData().add(agileAddRowsRequest);

        /*  Ignore Warnings when Code is already attached to a change
        addRowsRequestType.setDisableAllWarnings(true); */

            AddRowsResponseType addRowsResponseType = agileTableStub.addRows(addRowsRequestType);
            if (addRowsResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
                label_Utility_Status.setText("MCO Add Affected Item " + item.itemText + " SUCCESS");
                label_Utility_Status.getStyleClass().add("label-success");
            } else if (addRowsResponseType.getStatusCode().equals(ResponseStatusCode.WARNING)) {
                label_Utility_Status.setText("MCO Add Affected Item WARNING - " + item.itemText + " " +
                        addRowsResponseType.getWarnings().get(0).getWarning().get(0).getMessage());
                label_Utility_Status.getStyleClass().add("label-warning");
            } else {
                label_Utility_Status.setText("MCO Add Affected Item FAILURE - " + item.itemText + " " +
                        addRowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
                label_Utility_Status.getStyleClass().add("label-failure");
                agileMCO.itemText = null;
                return false;
            }

            /* Set LifeCycle Phase */

            UpdateRowsRequestType updateRowsRequestType = new UpdateRowsRequestType();
            AgileUpdateRowsRequest agileUpdateRowsRequest = new AgileUpdateRowsRequest();

            AgileUpdateRow updateRow = new AgileUpdateRow();
            updateRow.setRowId(getRowID("MCO", agileMCO.objectNumber,
                    ChangeConstants.TABLE_AFFECTEDITEMS.toString(), item.itemText, "itemNumber"));
            row = new AgileRowType();

            Element el_lifeCycle = createListElement("lifecyclePhase", "SD",
                    ChangeConstants.ATT_AFFECTED_ITEMS_LIFECYCLE_PHASE.toString());
            row.getAny().add(el_lifeCycle);
            updateRow.setRow(row);

            agileUpdateRowsRequest.getRow().add(updateRow);
            agileUpdateRowsRequest.setObjectInfo(table);

            updateRowsRequestType.getData().add(agileUpdateRowsRequest);
            UpdateRowsResponseType updateRowsResponseType = agileTableStub.updateRows(updateRowsRequestType);
            if (updateRowsResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
                label_Utility_Status.setText("Change Lifecycle" + item.itemText + " to SD SUCCESS");
                label_Utility_Status.getStyleClass().add("label-success");
            } else {
                label_Utility_Status.setText("Add Affected Item FAILURE - " + item.itemText + " " +
                        updateRowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
                label_Utility_Status.getStyleClass().add("label-failure");
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

        String workflow = String.valueOf(getWorkFlow("MCO", agileMCO.itemText, label_Utility_Status));
        agileSetWorkFlowRequestType.setWorkFlowIdentifier( workflow );

        setWorkFlowRequestType.getSetWorkFlowRequest().add(agileSetWorkFlowRequestType);
        SetWorkFlowResponseType setWorkflowResponseType = agileCollaborationStub.setWorkFlow(setWorkFlowRequestType);
        if (setWorkflowResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            label_Utility_Status.setText("MCO " + agileMCO.itemText + " Workflow  SUCCESS");
            label_Utility_Status.getStyleClass().add("label-success");
        }
        else{
            label_Utility_Status.setText("MCO " + agileMCO.itemText + " Workflow  FAILURE - " +
                    setWorkflowResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Utility_Status.getStyleClass().add("label-failure");
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
            label_Utility_Status.setText("MCO " + agileMCO.itemText + " Submit  SUCCESS");
            label_Utility_Status.getStyleClass().add("label-success");
        }
        else {
            label_Utility_Status.setText("MCO " + agileMCO.itemText + " Submit FAILURE - " +
                    setWorkflowResponseType.getExceptions().get(0).getException().get(0).getMessage());
            label_Utility_Status.getStyleClass().add("label-failure");
        }
    }

    public void ButtonApprovalStatusOnAction() {
        String taskQuery = "SELECT Id, Exception_Agile_ECO_MCO__c, Status, Subject, WhatId FROM Task WHERE Subject "
                + "Like '%FA' AND OwnerId = '" + ownerId + "' AND Status = 'In Progress'";

        ArrayList<String> approvers;
        Object[] names;

        try {
            /* FA Task Status */

            QueryResult tq = connection.query(taskQuery);
            SObject[] taskRecords = tq.getRecords();
            Task[] task = new Task[taskRecords.length];
            for (int i = 0; i < taskRecords.length; i++) {
                task[i] = (Task) taskRecords[i];
                String ECO_MCO = task[i].getException_Agile_ECO_MCO__c().substring(4);
                approvers = getApprovalStatus("MCO", ECO_MCO);
                names = approvers.toArray();

                Task updateTask = new Task();
                updateTask.setId(task[i].getId());
                StringBuilder description = new StringBuilder(dtf.format(LocalDateTime.now()));
                description.append("\nAwaiting approval from : \n");
                for (Object name: names) {
                    description.append(name.toString());
                    description.append(" ");
                }
                description.append("\n");
                updateTask.setDescription(description.toString());
                connection.update(new SObject[]{updateTask});
            }

            /* Revision Task Status */

            taskQuery = "SELECT Id, Exception_Agile_ECO_MCO__c, Status, Subject, WhatId FROM Task WHERE Subject "
                    + "Like '%%Revision%' AND OwnerId = '" + ownerId + "' AND Status = 'In Progress'";

            tq = connection.query(taskQuery);
            taskRecords = tq.getRecords();
            task = new Task[taskRecords.length];
            for (int i = 0; i < taskRecords.length; i++ ) {
                task[i] = (Task) taskRecords[i];
                String ECO_MCO = task[i].getException_Agile_ECO_MCO__c().substring(4);
                approvers = getApprovalStatus("ECO", ECO_MCO);
                names = approvers.toArray();

                Task updateTask = new Task();
                updateTask.setId(task[i].getId());
                StringBuilder description = new StringBuilder(dtf.format(LocalDateTime.now()));
                description.append("\nAwaiting approval from : \n");
                for (Object name : names) {
                    description.append(name.toString());
                    description.append(" ");
                }
                description.append("\n");
                updateTask.setDescription(description.toString());
                connection.update(new SObject[]{updateTask});
            }
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
        label_Utility_Status.setText("Approval Status update complete");
        label_Utility_Status.setTextFill(Color.GREEN);
    }
    private ArrayList<String> getApprovalStatus (String changeType, String changeNumber) {

        String current, approver;
        ArrayList<String> appStatus = new ArrayList<>();

        String objectNumber = agileSearchChange(changeType, changeNumber, label_Utility_Status);

        RequestTableType table = new RequestTableType();
        table.setClassIdentifier(changeType);
        table.setObjectNumber(objectNumber);
        table.setTableIdentifier("Workflow");

        LoadTableRequestType loadTableRequestType = new LoadTableRequestType();
        loadTableRequestType.getTableRequest().addAll(Arrays.asList(table));
        LoadTableResponseType loadTableResponseType = agileTableStub.loadTable(loadTableRequestType);

        AgileTableType workflow = loadTableResponseType.getTableContents().get(0);
        textAreaApprovals.appendText(changeType + " " + changeNumber + " awaiting approval from :\n");
        try {
            AgileRowType[] rows = workflow.getRow().toArray(new AgileRowType[0]);
            for (AgileRowType row : rows ) {
                List<Element> messages = row.getAny();
                if (messages.size() > 6) {
                    current = getMessageElementValue(messages.get(3));
                    if (current != null && current.length() > 8)
                        if (current.contains("Awaiting")) {
                            approver = getMessageElementValue(messages.get(5));
                            appStatus.add(approver);
                            textAreaApprovals.appendText(approver + "\n");
                        }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return appStatus;
    }
    public void ButtonQuitOnAction() {
        Stage stage = (Stage) button_Quit.getScene().getWindow();
        stage.close();
    }
}
