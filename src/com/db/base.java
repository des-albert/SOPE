package com.db;

import com.agile.ws.service.attachment.v1.jaxws.AttachmentPortType;
import com.agile.ws.service.attachment.v1.jaxws.AttachmentService;
import com.agile.ws.service.business.v1.jaxws.BusinessObjectPortType;
import com.agile.ws.service.business.v1.jaxws.BusinessObjectService;

import javax.xml.ws.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import com.agile.ws.service.collaboration.v1.jaxws.CollaborationPortType;
import com.agile.ws.service.collaboration.v1.jaxws.CollaborationService;
import com.agile.ws.service.metadata.v1.jaxws.AdminMetadataPortType;
import com.agile.ws.service.metadata.v1.jaxws.AdminMetadataService;
import com.agile.ws.service.search.v1.jaxws.SearchService;
import com.agile.ws.service.search.v1.jaxws.SearchService_Service;
import com.agile.ws.service.table.v1.jaxws.TablePortType;
import com.agile.ws.service.table.v1.jaxws.TableService;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import static com.db.sopeShared.*;


public class base {

    @FXML
    Label label_User, label_Version;
    @FXML
    Button buttonExceptions, buttonOpportunities, buttonAgile, buttonBaseQuit;

    private static final String VERSION = "V 3.3";

    private static String password;
    static String ownerId, agileCognizant, userFullName;
    static EnterpriseConnection connection;
    static BusinessObjectPortType agileBusinessObjectStub;
    static AttachmentPortType agileAttachmentStub;
    static AdminMetadataPortType agileAdminStub;
    static TablePortType agileTableStub;
    static CollaborationPortType agileCollaborationStub;
    static SearchService agileSearchStub;
    static ObservableList<String> manNameChoices, productTeams, analysts, prodLineChoices, priceCatChoices,
            prodFamilyChoices, bomClassChoices;
    private String agileServerURL, agileUsername;

    public void initialize() {
        wslogin();
        agileLogin();
    }

    private void wslogin() {
        String sfdcUsername, authEndPoint;
        agileECO = new agileItem();
        agileMCO = new agileItem();
        customCode = new agileItem();
        manPart  = new agileItem();
        redlineManPart  = new agileItem();

        String currentDirectory = System.getProperty("user.dir");
        final String version = VERSION;
        final String dataFile =  currentDirectory + "/UserData.txt";
        final String manufacturerFile = currentDirectory + "/manData.txt";

        try {
            FileReader fr = new FileReader(dataFile);
            BufferedReader br = new BufferedReader(fr);
            userFullName = br.readLine();
            sfdcUsername = br.readLine();
            password = br.readLine();

            ownerId = br.readLine();

            ConnectorConfig config = new ConnectorConfig();

            config.setUsername(sfdcUsername);
            config.setPassword(password);

            authEndPoint = br.readLine();
            config.setAuthEndpoint(authEndPoint);
            connection = new EnterpriseConnection(config);

            agileServerURL = br.readLine();
            if (agileServerURL.contains("dv"))
                label_Version.setText(version + "DV");
            else
                label_Version.setText(version);
            agileUsername = br.readLine();
            agileCognizant = br.readLine();

            br.close();
            fr.close();

            fr = new FileReader(manufacturerFile);
            br = new BufferedReader(fr);

            manNameChoices = FXCollections.observableArrayList();
            String line;
            while ((line = br.readLine()) != null) {
                manNameChoices.add(line);
            }

            br.close();
            fr.close();

            prodLineChoices = FXCollections.observableArrayList("CASCADE",
                    "CUSTOM ENGINEERING", "CCS", "SHASTA", "STORAGE");
            priceCatChoices = FXCollections.observableArrayList("CE-HW-CPG", "CE-HW-SCP",
                    "CE-HW-SPG", "CE-SW", "CE-INST", "SUPHW30", "SUPHW3P", "SUPHW3L", "SUPHW3M", "SUPHW3H","" +
                            "SUPSW30", "SUPSW3P", "SUPSW3L", "SUPSW3M", "SUPSW3H", "CE-SVC-PS", "CE-SVC-TNG");
            prodFamilyChoices = FXCollections.observableArrayList("CA", "CS", "DDN", "ES", "SNX", "STO", "XA");
            bomClassChoices = FXCollections.observableArrayList("ctBomBlade", "ctBomBladeOpt", "ctBomBoot",
                    "ctBomCabAux", "ctBomCabOp", "ctBomCabs", "ctBomDoors", "ctBomFacility", "ctBomFreight", "ctBomFte",
                    "ctBomFtePs", "ctBomHba", "ctBomHbaCables", "ctBomHbaSsd", "ctBomInstall", "ctBomInstallSw", "ctBomMem",
                    "ctBomNet", "ctBomPci", "ctBomPdc", "ctBomPdu", "ctBomProc", "ctBomProcGpu", "ctBomProcMic",
                    "ctBomProcOpt", "ctBomRackOpt", "ctBomRackPdu", "ctBomRacks", "ctBomServer", "ctBomServerChas",
                    "ctBomServerMb", "ctBomServerOpt", "ctBomStoCtrl", "ctBomStoCtrlDisk", "ctBomStoDsk", "ctBomStoDskSsd",
                    "ctBomStoTray", "ctBomStoTrayDisk", "ctBomSupFte", "ctBomSupHSML", "ctBomSupHw",
                    "ctBomSupSw", "ctBomSvc", "ctBomSw", "ctBomSwDBug", "ctBomSwFee", "ctBomSwitch", "ctBomSwitchOpt",
                    "ctBomSwOs", "ctBomSwPe", "ctBomSwWlm", "ctBomSys", "ctBomTapeLib", "ctBomTapeMedia", "ctBomTrng");
            analysts = FXCollections.observableArrayList("Erickson, Sue (sce)", "Wu, Bing (bing)");
            productTeams = FXCollections.observableArrayList("CPG", "SCP", "SPG");

        } catch (ConnectionException | IOException ce) {
            label_User.setText("Salesforce login failed");
            label_User.getStyleClass().add("label-failure");
        }
    }

    private void agileLogin() {

        try {
            String BUSINESS_OBJECT_URL = "/BusinessObject";
            URL url = new URL(agileServerURL + BUSINESS_OBJECT_URL + "?wsdl");
            BusinessObjectService businessLocator = new BusinessObjectService(url);
            if ((agileBusinessObjectStub = businessLocator.getBusinessObject()) == null)
                return;
            Map<String, Object> reqContextBusinessObject = ((BindingProvider) agileBusinessObjectStub).getRequestContext();
            reqContextBusinessObject.put(BindingProvider.USERNAME_PROPERTY, agileUsername);
            reqContextBusinessObject.put(BindingProvider.PASSWORD_PROPERTY, password);


            //  Collaboration
            String COLLABORATION_URL = agileServerURL + "/" + "Collaboration";
            url = new URL(COLLABORATION_URL + "?WSDL");
            CollaborationService collaborationLocator = new CollaborationService(url);
            if ((agileCollaborationStub = collaborationLocator.getCollaboration()) == null)
                return;
            Map<String, Object> reqContextCollaboration = ((BindingProvider) agileCollaborationStub).getRequestContext();
            reqContextCollaboration.put(BindingProvider.USERNAME_PROPERTY, agileUsername);
            reqContextCollaboration.put(BindingProvider.PASSWORD_PROPERTY, password);

            //  Attachment
            String ATTACHMENT_URL = agileServerURL + "/" + "Attachment";
            url = new URL(ATTACHMENT_URL + "?WSDL");
            AttachmentService attachmentLocator = new AttachmentService(url);
                if ((agileAttachmentStub = attachmentLocator.getAttachment()) == null)
                return;
            Map<String, Object> reqContextAttachment = ((BindingProvider) agileAttachmentStub).getRequestContext();
            reqContextAttachment.put(BindingProvider.USERNAME_PROPERTY, agileUsername);
            reqContextAttachment.put(BindingProvider.PASSWORD_PROPERTY, password);

            //  AdminMetaData
            String ADMIN_URL = agileServerURL + "/" + "AdminMetadata";
            url = new URL(ADMIN_URL + "?WSDL");
            AdminMetadataService adminLocator = new AdminMetadataService(url);
            if ((agileAdminStub = adminLocator.getAdminMetadata()) == null)
                return;
            Map<String, Object> reqContextAdmin = ((BindingProvider) agileAdminStub).getRequestContext();
            reqContextAdmin.put(BindingProvider.USERNAME_PROPERTY, agileUsername);
            reqContextAdmin.put(BindingProvider.PASSWORD_PROPERTY, password);

            //  Table
            String TABLE_URL = agileServerURL + "/" + "Table";
            url = new URL(TABLE_URL + "?WSDL");
            TableService tableLocator = new TableService(url);
            if ((agileTableStub = tableLocator.getTable()) == null)
                return;
            Map<String, Object> reqContextTable = ((BindingProvider) agileTableStub).getRequestContext();
            reqContextTable.put(BindingProvider.USERNAME_PROPERTY, agileUsername);
            reqContextTable.put(BindingProvider.PASSWORD_PROPERTY, password);

            //  Search
            String SEARCH_URL = agileServerURL + "/" + "Search";
            url = new URL(SEARCH_URL + "?WSDL");
            SearchService_Service searchLocator = new SearchService_Service(url);
            if ((agileSearchStub = searchLocator.getSearch()) == null)
                return;
            Map<String, Object> reqContextSearch = ((BindingProvider) agileSearchStub).getRequestContext();
            reqContextSearch.put(BindingProvider.USERNAME_PROPERTY, agileUsername);
            reqContextSearch.put(BindingProvider.PASSWORD_PROPERTY, password);

            label_User.setText(userFullName + " - login success");
            label_User.getStyleClass().add("label-success");

        } catch (IOException ce) {
            label_User.setText("Agile login failed");
            label_User.getStyleClass().add("label-failure");
        }
    }

    public void ButtonBaseQuitOnAction() {
        Platform.exit();
    }
    public void ButtonAgileOnAction() {
        try {
            FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("utility.fxml"));
            Parent partForm = fxmlFormLoader.load();
            Stage agileStage = new Stage();
            agileStage.setTitle("Agile Utilities");
            agileStage.setScene(new Scene(partForm));
            agileStage.show();
        } catch ( IOException ex) {
            ex.printStackTrace();
            label_User.setText("Utilities FormLoader failed");
            label_User.getStyleClass().add("label-success");
        }

    }
    public void ButtonOpportunitiesOnAction() {
        try {
            FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("opportunities.fxml"));
            Parent partForm = fxmlFormLoader.load();
            Stage exceptionStage = new Stage();
            exceptionStage.setTitle("Sales Operations Opportunities");
            exceptionStage.setScene(new Scene(partForm, 1000, 650));
            exceptionStage.show();
        } catch ( IOException ex) {
            ex.printStackTrace();
            label_User.setText("Opportunities FormLoader failed");
            label_User.getStyleClass().add("label-failure");
        }
    }

    public void ButtonExceptionsOnAction() {
        try {
            FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("exceptions.fxml"));
            Parent partForm = fxmlFormLoader.load();
            Stage exceptionStage = new Stage();
            exceptionStage.setTitle("Sales Operations Exceptions");
            exceptionStage.setScene(new Scene(partForm, 1820, 650));
            exceptionStage.show();
         } catch ( IOException ex) {
            ex.printStackTrace();
            label_User.setText("Exceptions FormLoader failed");
            label_User.getStyleClass().add("label-failure");
        }
    }

}