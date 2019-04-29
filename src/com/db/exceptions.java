package com.db;


import com.agile.ws.schema.collaboration.v1.jaxws.*;
import com.agile.ws.schema.common.v1.jaxws.*;
import com.agile.ws.schema.table.v1.jaxws.LoadTableRequestType;
import com.agile.ws.schema.table.v1.jaxws.LoadTableResponseType;
import com.agile.ws.schema.table.v1.jaxws.RequestTableType;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.Exception__c;
import com.sforce.soap.enterprise.sobject.Opportunity;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.soap.enterprise.sobject.Task;
import com.sforce.ws.ConnectionException;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.w3c.dom.Element;


import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static com.db.sopeShared.*;
import static com.db.base.*;
import static org.apache.commons.lang3.StringUtils.chop;

public class exceptions  {

    private static ObservableList<exDisplay> data = FXCollections.observableArrayList();
    @FXML
    Label labelStatus_Message;
    @FXML
    Button buttonExceptionQuit;
    @FXML
    TableView<exDisplay> tableViewExceptions;
    @FXML
    TableColumn<exDisplay, String> columnOpp, columnName, columnDate, columnStatus, columnNumber, columnAgileId,
            columnECO_MCO, columnApprovers, columnPrelim, columnFA, columnRevise, columnCodes;
    private Exception__c [] exception;
    static String exceptionName, agileId;
    private static String exceptionId, prelimTaskId, firstArticleTaskId, reviseTaskId;
    private static int rowSelect;
    private HashMap<String, String>  mapPrelim, mapFA, mapRevise;
    private DateTimeFormatter dtf;
    private String taskECOMCO, approvers;

    public void initialize() {

        dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        columnOpp.setCellValueFactory(new PropertyValueFactory<>("exOpp"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("exName"));
        columnDate.setCellValueFactory(new PropertyValueFactory<>("exDate"));
        columnStatus.setCellValueFactory(new PropertyValueFactory<>("exStatus"));
        columnNumber.setCellValueFactory(new PropertyValueFactory<>("exNumber"));
        columnAgileId.setCellValueFactory(new PropertyValueFactory<>("exAgileId"));
        columnECO_MCO.setCellValueFactory(new PropertyValueFactory<>("exECO_MCO"));
        columnApprovers.setCellValueFactory(new PropertyValueFactory<>("exApprovers"));
        columnPrelim.setCellValueFactory(new PropertyValueFactory<>("exPrelim"));
        columnPrelim.setCellValueFactory(new PropertyValueFactory<>("exPrelim"));
        columnFA.setCellValueFactory(new PropertyValueFactory<>("exFA"));
        columnRevise.setCellValueFactory(new PropertyValueFactory<>("exRevise"));
        columnCodes.setCellValueFactory(new PropertyValueFactory<>("exCodes"));



        setUp();
        load_Exceptions();
    }

    private void setUp() {

        final int EXCEPTION_LINK_COLUMN = 1;
        final int EXCEPTION_PRELIM_COLUMN = 8;
        final int EXCEPTION_FA_COLUMN = 9;
        final int EXCEPTION_REVISION_COLUMN = 10;

        columnName.setCellFactory(e -> new TableCell<exDisplay, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty)
                    setText(null);

                else {
                    setText(item);
                    this.setStyle("-fx-text-fill: blue;");
                }
            }
        });

        columnPrelim.setCellFactory(e -> new TableCell<exDisplay, String> () {
            @Override
            public void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);
                if (item == null || empty)
                    setText(null);
                else {
                    setText(item);
                    switch (item) {
                        case "Not Started":
                            this.setStyle("-fx-text-fill: red;");
                            break;
                        case "In Progress":
                            this.setStyle("-fx-text-fill: orange;");
                            break;
                        case "Completed":
                            this.setStyle("-fx-text-fill: green;");
                            break;
                    }
                }
            }
        });

        columnFA.setCellFactory(e -> new TableCell<exDisplay, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty)
                    setText(null);
                else {
                    setText(item);
                    switch (item) {
                        case "Not Started":
                            this.setStyle("-fx-text-fill: red;");
                            break;
                        case "In Progress":
                            this.setStyle("-fx-text-fill: orange;");
                            break;
                        case "Completed":
                            this.setStyle("-fx-text-fill: green;");
                            break;
                    }
                }
            }
        });
        columnRevise.setCellFactory(e -> new TableCell<exDisplay, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty)
                    setText(null);
                else {
                    setText(item);
                    switch (item) {
                        case "Not Started":
                            this.setStyle("-fx-text-fill: red;");
                            break;
                        case "In Progress":
                            this.setStyle("-fx-text-fill: orange;");
                            break;
                        case "Completed":
                            this.setStyle("-fx-text-fill: green;");
                            break;
                    }
                }
            }
        });

        columnCodes.setCellFactory(tc ->  {
            TableCell<exDisplay, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(columnCodes.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell ;

        });

        /* Handle row selection */

        tableViewExceptions.getSelectionModel().setCellSelectionEnabled(true);
        ObservableList<TablePosition> selectedCells = tableViewExceptions.getSelectionModel().getSelectedCells();

        selectedCells.addListener((ListChangeListener<TablePosition>) change -> {
            TablePosition tablePosition = selectedCells.get(0);
            rowSelect = tablePosition.getRow();
            int column = tablePosition.getColumn();
            exceptionId = exception[rowSelect].getId();
            exceptionName = exception[rowSelect].getException_Number__c();
            agileId = exception[rowSelect].getAgile_Opportunity_ID__c();

            switch (column) {
                case EXCEPTION_LINK_COLUMN: {
                    String link = "https://cray.my.salesforce.com/" + exceptionId;
                    openWebpage(link);
                }
                break;

                /* Create Preliminary Custom Code */

                case EXCEPTION_PRELIM_COLUMN: {
                    if (mapPrelim.containsKey(exceptionId)) {
                        prelimTaskId = getKeyId(mapPrelim.get(exceptionId));
                        if (getKeyStatus(mapPrelim.get(exceptionId)).equals("Not Started")) {
                            try {
                                FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("prelim.fxml"));
                                Parent prelimForm = fxmlFormLoader.load();
                                Stage prelimStage = new Stage();
                                prelimStage.setTitle("New Custom Code");
                                prelimStage.setScene(new Scene(prelimForm));
                                prelimStage.show();
                                prelimStage.setOnHiding(e -> updatePreliminaryStatus());
                            } catch (IOException ex) {
                                labelStatus_Message.setText("preliminary FXML Loader Exception");
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Task Complete Confirmation");
                            alert.setHeaderText("Preliminary Task");
                            alert.setContentText("Change Task to Completed ?");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() ) {
                                if (result.get() == ButtonType.OK) {
                                    try {
                                        Task updateTask = new Task();
                                        updateTask.setId(prelimTaskId);
                                        updateTask.setStatus("Completed");
                                        if (updateTask.getDescription() == null)
                                            updateTask.setDescription("Complete - " + dtf.format(LocalDateTime.now()));
                                        else
                                            updateTask.setDescription("Complete - " + dtf.format(LocalDateTime.now())
                                                    + "\n" + updateTask.getDescription());
                                        connection.update(new SObject[]{updateTask});
                                        exDisplay row = data.get(rowSelect);
                                        row.setExPrelim("Completed");
                                        tableViewExceptions.refresh();
                                    } catch (ConnectionException ex) {
                                        labelStatus_Message.setText("Preliminary Task Update Exception");
                                        labelStatus_Message.getStyleClass().add("label-success");
                                    }
                                }
                            }
                        }
                    }
                }
                break;

                /* Change Custom Code to First Article ( FA ) */

                case EXCEPTION_FA_COLUMN: {
                    if (mapFA.containsKey(exceptionId)) {
                        prelimTaskId = getKeyId(mapPrelim.get(exceptionId));
                        firstArticleTaskId = getKeyId(mapFA.get(exceptionId));
                        agileECO.itemText = getKeyAgile(mapPrelim.get(exceptionId)).substring(4);
                        if (getKeyStatus(mapFA.get(exceptionId)).equals("Not Started")) {
                            try {
                                FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("firstArticle.fxml"));
                                Parent firstArticleForm = fxmlFormLoader.load();
                                Stage faStage = new Stage();
                                faStage.setTitle("Promote Custom Code to first Article ");
                                faStage.setScene(new Scene(firstArticleForm, 600, 450));
                                faStage.show();
                                faStage.setOnHiding(e -> updateFirstArticleStatus());
                            }
                            catch (IOException ex) {
                                labelStatus_Message.setText("first Article FXML Loader Exception");
                                labelStatus_Message.getStyleClass().add("label-failure");
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Task Complete Confirmation");
                            alert.setHeaderText("first Article Task");
                            alert.setContentText("Change Task to Completed ?");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent()) {
                                if (result.get() == ButtonType.OK) {
                                    try {
                                        Task updateTask = new Task();
                                        updateTask.setId(firstArticleTaskId);
                                        updateTask.setStatus("Completed");
                                        if (updateTask.getDescription() == null)
                                            updateTask.setDescription("Complete - " + dtf.format(LocalDateTime.now()));
                                        else
                                            updateTask.setDescription("Complete - " + dtf.format(LocalDateTime.now())
                                                    + "\n" + updateTask.getDescription());
                                        connection.update(new SObject[]{updateTask});
                                        exDisplay row = data.get(rowSelect);
                                        row.setExFA("Completed");
                                        tableViewExceptions.refresh();
                                    } catch (ConnectionException ex) {
                                        labelStatus_Message.setText("first Article Task Update Exception");
                                        labelStatus_Message.getStyleClass().add("label-failure");
                                    }
                                }
                            }
                        }
                    }
                }

                break;

                case EXCEPTION_REVISION_COLUMN: {
                    if (mapRevise.containsKey(exceptionId)) {
                        reviseTaskId = getKeyId(mapRevise.get(exceptionId));
                        if (getKeyStatus(mapRevise.get(exceptionId)).equals("Not Started")) {
                            try {
                                FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("revise.fxml"));
                                Parent reviseForm = fxmlFormLoader.load();
                                Stage reviseStage = new Stage();
                                reviseStage.setTitle("Revise Quote ");
                                reviseStage.setScene(new Scene(reviseForm, 925, 700));
                                reviseStage.show();
                                reviseStage.setOnHiding(e -> updateReviseStatus());
                            }
                            catch (IOException ex) {
                                labelStatus_Message.setText("Revision FXML Loader Exception");
                                labelStatus_Message.getStyleClass().add("label-failure");
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Task Complete Confirmation");
                            alert.setHeaderText("Revision Task");
                            alert.setContentText("Change Task to Completed ?");

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() ) {
                                if (result.get() == ButtonType.OK) {
                                    try {
                                        Task updateTask = new Task();
                                        updateTask.setId(reviseTaskId);
                                        updateTask.setStatus("Completed");
                                        if (updateTask.getDescription() == null)
                                            updateTask.setDescription("Complete - " + dtf.format(LocalDateTime.now()));
                                        else
                                            updateTask.setDescription("Complete - " + dtf.format(LocalDateTime.now())
                                                    + "\n" + updateTask.getDescription());
                                        connection.update(new SObject[]{updateTask});
                                        exDisplay row = data.get(rowSelect);
                                        row.setExPrelim("Completed");
                                        tableViewExceptions.refresh();
                                    } catch (ConnectionException ex) {
                                        labelStatus_Message.setText("Preliminary Task Update Exception");
                                        labelStatus_Message.getStyleClass().add("label-failure");
                                    }
                                }
                            }
                        }
                    }

                }
                break;
            }       // end switch
        });
    }

    private void load_Exceptions() {
        String sfdcPrelim, sfdcFA, sfdcRevise, changeStatus;
        Task[] task;
        data.clear();
        tableViewExceptions.setItems(data);

        StringBuilder soqlQuery = new StringBuilder("SELECT Id, Name, Agile_Opportunity_ID__c, Exception_Decision_Due__c, Opportunity_Name__c, "
                + "Status__c, Exception_Number__c, Quoting_Instructions__c FROM Exception__c "+ "WHERE Id IN ('");

        StringBuilder outerQuery = new StringBuilder("SELECT Id, Name FROM Opportunity WHERE Id IN ('");
        String prelimQuery = "SELECT Id, Exception_Agile_ECO_MCO__c, Status, Subject, WhatId FROM Task WHERE Subject "
                + "LIKE '%Preliminary' AND OwnerId = '" + ownerId + "'";
        String faQuery = "SELECT Id, Exception_Agile_ECO_MCO__c, Status, Subject, WhatId FROM Task WHERE Subject "
                + "LIKE '%FA' AND OwnerId = '" + ownerId + "'";
        String reviseQuery = "SELECT Id, Exception_Agile_ECO_MCO__c, Status, Subject, WhatId FROM Task WHERE Subject "
                + "LIKE '%Revision%' AND OwnerId = '" + ownerId + "'";

        QueryResult qr;
        Opportunity[] opp;

        HashMap<String, String> mapOppName = new HashMap<>();
        mapPrelim = new HashMap<>();
        mapFA = new HashMap<>();
        mapRevise = new HashMap<>();
        ArrayList<String> exList = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yy");

        /* Select Active Tasks and store status  */

        try {
            /* Revision Task Status */

            String exId, taskStatus;
            QueryResult tq = connection.query(reviseQuery);
            SObject[] taskRecords = tq.getRecords();
            task = new Task[taskRecords.length];
            for (int i = 0; i < taskRecords.length; i++) {
                task[i] = (Task) taskRecords[i];
                exId = task[i].getWhatId();
                taskStatus = task[i].getStatus();
                if (taskStatus.equals("Not Started") || taskStatus.equals("In Progress")) {
                    mapRevise.put(exId, MakeKey(task[i].getId(), taskStatus, task[i].getException_Agile_ECO_MCO__c()));
                    exList.add(exId);
                }
            }

            /* FA Task Status */

            tq = connection.query(faQuery);
            taskRecords = tq.getRecords();
            task = new Task[taskRecords.length];
            for (int i = 0; i < taskRecords.length; i++) {
                task[i] = (Task) taskRecords[i];
                exId = task[i].getWhatId();
                taskStatus = task[i].getStatus();
                if (taskStatus.equals("Not Started") || taskStatus.equals("In Progress") |
                        (taskStatus.equals("Completed") && exList.contains(exId))) {
                    mapFA.put(exId, MakeKey(task[i].getId(), task[i].getStatus(), task[i].getException_Agile_ECO_MCO__c()));
                    if (!exList.contains(exId))
                        exList.add(exId);
                }
            }

            /* Preliminary Task Status */

            tq = connection.query(prelimQuery);
            taskRecords = tq.getRecords();
            task = new Task[taskRecords.length];
            for (int i = 0; i < taskRecords.length; i++) {
                task[i] = (Task) taskRecords[i];
                exId = task[i].getWhatId();
                taskStatus = task[i].getStatus();
                if (taskStatus.equals("Not Started") | taskStatus.equals("In Progress") |
                        (taskStatus.equals("Completed") && exList.contains(exId))) {
                    mapPrelim.put(exId, MakeKey(task[i].getId(), task[i].getStatus(), task[i].getException_Agile_ECO_MCO__c()));
                    if (!exList.contains(exId))
                        exList.add(exId);
                }
            }

            for(int i = 0; i < exList.size(); i++) {
                if (i == 0)
                    soqlQuery.append(exList.get(i));
                else {
                    soqlQuery.append("', '");
                    soqlQuery.append(exList.get(i));
                }
            }
            soqlQuery.append("') AND Status__c <> 'Cancelled' AND RecordTypeId = '0120b000000lBV8AAM' ORDER BY Opportunity_Name__c");

            qr = connection.query(soqlQuery.toString());
            if (qr.getSize() > 0) {
                SObject[] records = qr.getRecords();
                exception = new Exception__c[records.length];
                for (int i = 0; i < records.length; i++) {
                    exception[i] = (Exception__c) records[i];
                    if (i == 0)
                        outerQuery.append(exception[0].getOpportunity_Name__c());
                    else {
                        outerQuery.append("', '");
                        outerQuery.append(exception[i].getOpportunity_Name__c());
                    }
                }
                outerQuery.append("') ORDER BY Id");
                QueryResult iq = connection.query(outerQuery.toString());
                SObject[] oppRecords = iq.getRecords();
                opp = new Opportunity[oppRecords.length];

                for (int i = 0; i < oppRecords.length; i++) {
                    opp[i] = (Opportunity) oppRecords[i];
                    if (!mapOppName.containsKey(opp[i].getId()))
                        mapOppName.put(opp[i].getId(), opp[i].getName());
                }
                for (int i = 0; i < records.length; i++) {
                    sfdcPrelim = null;
                    sfdcFA = null;
                    sfdcRevise =  null;
                    approvers = null;
                    if (mapPrelim.containsKey(exception[i].getId())) {
                        taskECOMCO = getKeyAgile(mapPrelim.get(exception[i].getId()));
                        sfdcPrelim = getKeyStatus(mapPrelim.get(exception[i].getId()));
                        if (sfdcPrelim.equals("In Progress")) {
                            taskECOMCO = taskECOMCO + "\n" +
                                getKeyStatus(getChangeStatus(taskECOMCO.substring(4), "ECO"));

                        }
                    }
                    if (mapFA.containsKey(exception[i].getId())) {
                        taskECOMCO = getKeyAgile(mapFA.get(exception[i].getId()));
                        sfdcFA = getKeyStatus(mapFA.get(exception[i].getId()));
                        if (sfdcFA.equals("In Progress")) {
                            changeStatus = getChangeStatus(taskECOMCO.substring(4), "MCO");
                            if (changeStatus != null) {
                                taskECOMCO = taskECOMCO + "\n" + getKeyStatus(changeStatus);
                                approvers = getChangeApprovers(getKeyId(changeStatus), "MCO");
                            }
                            else {
                                taskECOMCO = taskECOMCO + "\n" + " Status not found";
                            }
                        }
                        else {
                            taskECOMCO = getKeyAgile(mapPrelim.get(exception[i].getId()));
                        }
                    }
                    if (mapRevise.containsKey(exception[i].getId())) {
                        taskECOMCO = getKeyAgile(mapRevise.get(exception[i].getId()));
                        sfdcRevise = getKeyStatus(mapRevise.get(exception[i].getId()));
                        if (sfdcRevise.equals("In Progress")) {
                            changeStatus = getChangeStatus(taskECOMCO.substring(4), "ECO");
                            if (changeStatus != null) {
                                taskECOMCO = taskECOMCO + "\n" + getKeyStatus(changeStatus);
                                approvers = getChangeApprovers(getKeyId(changeStatus), "ECO");
                            }
                            else {
                                taskECOMCO = taskECOMCO + "\n" + " Status not found";
                            }
                        }

                    }
                    String oppName = mapOppName.get(exception[i].getOpportunity_Name__c());
                    String instructions = exception[i].getQuoting_Instructions__c();
                    if(instructions != null) {
                        instructions = instructions.replaceAll("<br>", ",");
                        instructions = chop(instructions);
                    }

                    exDisplay row = new exDisplay(oppName, exception[i].getName(),
                            formatter.format(exception[i].getException_Decision_Due__c().getTime()),
                            exception[i].getStatus__c(), exception[i].getException_Number__c(),
                            exception[i].getAgile_Opportunity_ID__c(), taskECOMCO, approvers, sfdcPrelim, sfdcFA, sfdcRevise,
                            instructions);
                    data.add(row);
                }
            }
            tableViewExceptions.refresh();

        } catch (ConnectionException ce) {
            ce.printStackTrace();
        }
    }

    private void updatePreliminaryStatus() {
        if (agileECO.itemText != null) {
            StringBuilder quoteParts = new StringBuilder();
            try {
                Task updateTask = new Task();
                updateTask.setId(prelimTaskId);
                updateTask.setStatus("In Progress");
                updateTask.setDescription("ECO " + agileECO.itemText + " created - "
                                + dtf.format(LocalDateTime.now()) + "\n");
                updateTask.setException_Agile_ECO_MCO__c("ECO " + agileECO.itemText);
                connection.update(new SObject[]{updateTask});

                for (agileItem item : customCodeList) {
                    quoteParts.append(item.itemText);
                    quoteParts.append("<br>");

                }
                Exception__c updateException = new Exception__c();
                updateException.setId(exceptionId);
                updateException.setQuoting_Instructions__c(quoteParts.toString());
                connection.update(new SObject[]{updateException});

                exDisplay row = data.get(rowSelect);
                row.setExPrelim("In Progress");
                row.setExECO_MCO("ECO " + agileECO.itemText + " " +
                        getKeyStatus(getChangeStatus(agileECO.itemText, "ECO")));
                row.setExCodes(quoteParts.toString());
                tableViewExceptions.refresh();

            } catch (ConnectionException ex) {
                labelStatus_Message.setText("update Preliminary Status Error");
                labelStatus_Message.getStyleClass().add("label-failure");
            }
        }
    }

    private void updateFirstArticleStatus() {
        if (agileMCO.itemText != null) {
            try {
                Task updateTask = new Task();
                updateTask.setId(firstArticleTaskId);
                updateTask.setStatus("In Progress");
                updateTask.setDescription("MCO " + agileMCO.itemText + " created - " +
                                dtf.format(LocalDateTime.now()) + "\n");
                updateTask.setException_Agile_ECO_MCO__c("MCO " + agileMCO.itemText);
                connection.update(new SObject[]{updateTask});

                exDisplay row = data.get(rowSelect);
                row.setExFA("In Progress");
                if (agileMCO.itemText.equals(agileECO.itemText))
                    row.setExECO_MCO("ECO " + agileECO.itemText + " " +
                            getKeyStatus(getChangeStatus(agileECO.itemText, "ECO")));
                else
                    row.setExECO_MCO("MCO " + agileMCO.itemText + " " +
                            getKeyStatus(getChangeStatus(agileMCO.itemText, "MCO")));
                tableViewExceptions.refresh();

            } catch (ConnectionException ex) {
                labelStatus_Message.setText("update first Article Status Error");
                labelStatus_Message.getStyleClass().add("label-failure");
            }
        }
    }
    private void updateReviseStatus() {
        if (agileECO.itemText != null) {
            try {
                Task updateTask = new Task();
                updateTask.setId(reviseTaskId);
                updateTask.setStatus("In Progress");
                updateTask.setDescription("ECO " + agileECO.itemText + " created - " +
                        dtf.format(LocalDateTime.now()) + "\n");
                updateTask.setException_Agile_ECO_MCO__c("ECO " + agileECO.itemText);
                connection.update(new SObject[]{updateTask});

                exDisplay row = data.get(rowSelect);
                row.setExRevise("In Progress");
                row.setExECO_MCO("ECO " + agileECO.itemText + " " +
                        getKeyStatus(getChangeStatus(taskECOMCO.substring(4), "ECO")));
                tableViewExceptions.refresh();

            } catch (ConnectionException ex) {
                labelStatus_Message.setText("update revision Status Error");
                labelStatus_Message.getStyleClass().add("label-failure");
            }
        }
    }

    public void ButtonExceptionQuitOnAction() {
        Stage stage = (Stage) buttonExceptionQuit.getScene().getWindow();
        stage.close();
    }
    private String MakeKey(String id, String status, String agile) {
        return id + '~' + status + '^' + agile;
    }
    private String getKeyId(String a) {
        return a.substring(0, a.indexOf('~'));
    }
    private String getKeyStatus(String a)
    {
        return a.substring(a.indexOf('~') + 1, a.indexOf('^'));
    }
    private String getKeyAgile(String a) {
        return a.substring(a.indexOf('^') + 1);
    }

    static void openWebpage(String url) {
        try {
            Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.browse(new URL(url).toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* find status of Agile PLM Change */

    private String getChangeStatus (String changeNumber, String changeType) {
        String changeStatus, objId;
        GetStatusRequestType getStatusRequestType = new GetStatusRequestType();
        AgileGetStatusRequestType agileGetStatusRequestType = new AgileGetStatusRequestType();

        agileGetStatusRequestType.setClassIdentifier(changeType);
        agileGetStatusRequestType.setObjectNumber(changeNumber);

        getStatusRequestType.getStatusRequest().add(agileGetStatusRequestType);
        GetStatusResponseType getStatusResponseType = agileCollaborationStub.getStatus(getStatusRequestType);
        if (getStatusResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            AgileGetStatusResponseType response = getStatusResponseType.getStatusResponse().get(0);
            changeStatus = response.getCurrentStatus().getStatusDisplayName();
            objId =  response.getIdentifier().getObjectId().toString();
            AgileStatusType nextDefaultStatus = response.getNextDefaultStatus();
            if (nextDefaultStatus != null) {
                AgileStatusType[] nextValidStatuses = response.getNextStatus().toArray(new AgileStatusType[0]);
                for (AgileStatusType nextStatus : nextValidStatuses)
                    changeStatus = nextStatus.getStatusDisplayName();
            }
            return MakeKey(objId, changeStatus, null);
        }
        labelStatus_Message.setText("Change Status Request Error");
        labelStatus_Message.getStyleClass().add("label-failure");
        return null;
    }
    private String getChangeApprovers (String objNumber, String change) {
        String current, approvers;
        StringBuilder appStatus = new StringBuilder();

        RequestTableType table = new RequestTableType();
        table.setClassIdentifier(change);
        table.setObjectNumber(objNumber);
        table.setTableIdentifier("Workflow");

        LoadTableRequestType loadTableRequestType = new LoadTableRequestType();
        loadTableRequestType.getTableRequest().addAll(Arrays.asList(table));
        LoadTableResponseType loadTableResponseType = agileTableStub.loadTable(loadTableRequestType);

        if(loadTableResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {

            AgileTableType workflow = loadTableResponseType.getTableContents().get(0);
            try {
                AgileRowType[] rows = workflow.getRow().toArray(new AgileRowType[0]);
                for (int j = 0; j < rows.length; j++) {
                    List<Element> messages = rows[j].getAny();
                    if (messages.size() > 6) {
                        current = getMessageElementValue(messages.get(3));
                        if (current != null && current.length() > 8)
                            if (current.contains("Awaiting")) {
                                approvers = getMessageElementValue(messages.get(5));
                                appStatus.append(approvers);
                                appStatus.append("\n");
                            }
                    }
                }
            } catch (NullPointerException e) {
                labelStatus_Message.setText("Change workflow error");
                labelStatus_Message.getStyleClass().add("label-failure");
            }
            return appStatus.toString();
        }
        else
            return null;
    }
}

