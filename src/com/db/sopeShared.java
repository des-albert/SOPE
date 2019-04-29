package com.db;

import com.agile.api.ChangeConstants;
import com.agile.api.ItemConstants;
import com.agile.ws.schema.business.v1.jaxws.*;
import com.agile.ws.schema.collaboration.v1.jaxws.*;
import com.agile.ws.schema.common.v1.jaxws.*;
import com.agile.ws.schema.metadata.v1.jaxws.AgileGetAutoNumbersRequestType;
import com.agile.ws.schema.metadata.v1.jaxws.AgileGetAutoNumbersResponseType;
import com.agile.ws.schema.metadata.v1.jaxws.GetAutoNumbersRequestType;
import com.agile.ws.schema.metadata.v1.jaxws.GetAutoNumbersResponseType;
import com.agile.ws.schema.search.v1.jaxws.QuickSearchRequestType;
import com.agile.ws.schema.search.v1.jaxws.QuickSearchResponseType;
import com.agile.ws.schema.table.v1.jaxws.LoadTableRequestType;
import com.agile.ws.schema.table.v1.jaxws.LoadTableResponseType;
import com.agile.ws.schema.table.v1.jaxws.RequestTableType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.TextField;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.db.base.*;

public class sopeShared {

    static agileItem agileECO, agileMCO, customCode, manPart, redlineManPart;

    static List<agileItem> customCodeList;

    private static final String COMMONNAMESPACEURI = "http://xmlns.oracle.com/AgileObjects/Core/Common/V1";

    static agileItem agileSearchPart(String keyword, Label status) {
        agileItem item = new agileItem();
        QuickSearchRequestType quickSearchRequestType = new QuickSearchRequestType();
        quickSearchRequestType.setClassIdentifier("ManufacturerPart");
        quickSearchRequestType.setKeywords(keyword);
        quickSearchRequestType.setSearchFiles(false);
        QuickSearchResponseType quickSearchResponseType = agileSearchStub.quickSearch(quickSearchRequestType);

        if (quickSearchResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            String partNumber = null;
            String manName = null;
            List<Element> elementList = quickSearchResponseType.getTable().getRow().get(0).getAny();
            for (Element e: elementList) {
                if (e.getTagName().equals("ManufacturerPartNumber")) {
                    partNumber = e.getTextContent();
                    partNumber = partNumber.substring(0, partNumber.indexOf('(') - 1);
                }
                else if (e.getTagName().equals("ManufacturerName")) {
                    manName = e.getTextContent();
                }
            }
            if (partNumber.equals(keyword)) {
                status.setText("ManufacturerPart"+ " - " + keyword + " Search  SUCCESS");
                status.getStyleClass().add("label-success");
                item.objectNumber = quickSearchResponseType.getTable().getRow().get(0).getObjectReferentId().getObjectId().toString();
                item.itemText = manName;
                return item;
            }
        }
        status.setText("ManufacturerPart" + " - " + keyword + " Search  FAILURE " +
                quickSearchResponseType.getExceptions().get(0).getException().get(0).getMessage());
        status.getStyleClass().add("label-failure");

        return null;

    }

    static String agileSearchCode(String partType, String keyword, Label status) {
        QuickSearchRequestType quickSearchRequestType = new QuickSearchRequestType();
        quickSearchRequestType.setClassIdentifier(partType);
        quickSearchRequestType.setKeywords(keyword);
        quickSearchRequestType.setSearchFiles(false);

        QuickSearchResponseType quickSearchResponseType = agileSearchStub.quickSearch(quickSearchRequestType);

        if (quickSearchResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            String code = null;
            List<Element> elementList = quickSearchResponseType.getTable().getRow().get(0).getAny();
            for (Element e: elementList) {
                if (e.getTagName().equals("Number")) {
                    code = e.getTextContent();
                }
            }
            if (code.equals(keyword)) {
                status.setText("Code " + " - " + keyword + " Search  SUCCESS");
                status.getStyleClass().add("label-success");
                return quickSearchResponseType.getTable().getRow().get(0).getObjectReferentId().getObjectId().toString();
            }
        }
        status.setText("Code" + " - " + keyword + " Search  FAILURE " +
                quickSearchResponseType.getExceptions().get(0).getException().get(0).getMessage());
        status.getStyleClass().add("label-failure");

        return null;

    }

    static String agileSearchChange(String changeType, String keyword, Label status) {
        QuickSearchRequestType quickSearchRequestType = new QuickSearchRequestType();
        quickSearchRequestType.setClassIdentifier(changeType);
        quickSearchRequestType.setKeywords(keyword);
        quickSearchRequestType.setSearchFiles(false);
        QuickSearchResponseType quickSearchResponseType = agileSearchStub.quickSearch(quickSearchRequestType);

        if (quickSearchResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            String changeNumber = null;
            List<Element> elementList = quickSearchResponseType.getTable().getRow().get(0).getAny();
            for (Element e: elementList) {
                if (e.getTagName().equals("Number")) {
                    changeNumber = e.getTextContent();
                }
            }
            if (changeNumber.equals(keyword)) {
                status.setText("Change " + " - " + keyword + " Search  SUCCESS");
                status.getStyleClass().add("label-success");
                return quickSearchResponseType.getTable().getRow().get(0).getObjectReferentId().getObjectId().toString();
            }
        }
        status.setText("Change" + " - " + keyword + " Search  FAILURE " +
                quickSearchResponseType.getExceptions().get(0).getException().get(0).getMessage());
        status.getStyleClass().add("label-failure");

        return null;

    }

    static ArrayList<agileItem> agileAffectedItems(String object, Label status) {
        ArrayList<agileItem> parts = new ArrayList<>();
        GetObjectRequestType getObjectRequestType = new GetObjectRequestType();
        AgileGetObjectRequest agileGetObjectRequest = new AgileGetObjectRequest();
        agileGetObjectRequest.setClassIdentifier("ECO");
        agileGetObjectRequest.setObjectNumber(object);
        AgileDataTableRequestType tableRequests = new AgileDataTableRequestType();
        tableRequests.setTableIdentifier(ChangeConstants.TABLE_AFFECTEDITEMS.toString() );
        tableRequests.setLoadCellMetaData(false);
        agileGetObjectRequest.getTableRequests().add(tableRequests);
        getObjectRequestType.getRequests().add(agileGetObjectRequest);

        GetObjectResponseType getObjectResponseType = agileBusinessObjectStub.getObject(getObjectRequestType);

        if (getObjectResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            status.setText("ECO" + " - " + agileECO.itemText + " Search  SUCCESS");
            status.getStyleClass().add("label-success");
            AgileGetObjectResponse response = getObjectResponseType.getResponses().get(0);
            AgileObjectType agileObject = response.getAgileObject();
            AgileTableType table = agileObject.getTable().get(0);
            AgileRowType rows[] = table.getRow().toArray(new AgileRowType[0]);

            String itemNumber = null;
            String itemObjectNumber;
            for (AgileRowType row : rows) {
                itemObjectNumber = row.getObjectReferentId().getObjectId().toString();
                List<Element> messages = row.getAny();
                for (Element e : messages)
                    if (e.getTagName().equals("itemNumber"))
                        itemNumber = e.getTextContent();
                parts.add(new agileItem(itemNumber, itemObjectNumber));
            }
            return parts;

        }
        else {
            status.setText("ECO" + " - " + object + " Search  FAILURE " +
                    getObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
            status.getStyleClass().add("label-failure");
        }
        return null;
    }

    static Integer getRowID(String cls, String objectNum, String tableId, String childNum, String tag ) {
        LoadTableRequestType loadTableRequestType = new LoadTableRequestType();
        RequestTableType table = new RequestTableType();
        table.setClassIdentifier( cls );
        table.setObjectNumber(objectNum);
        table.setTableIdentifier( tableId );
        loadTableRequestType.getTableRequest().add(table);
        Integer rowID = null;

        LoadTableResponseType loadTableResponseType = agileTableStub.loadTable(loadTableRequestType);
        if( loadTableResponseType.getStatusCode().toString().equals( ResponseStatusCode.SUCCESS.value() ) ){
            AgileTableType[] tables = loadTableResponseType.getTableContents().toArray(new AgileTableType[0]);
            for (AgileTableType tb : tables ) {
                AgileRowType[] rows = tb.getRow().toArray(new AgileRowType[0]);
                for (AgileRowType row : rows) {
                    List<Element> messages = row.getAny();
                    for (Element element : messages) {
                        if (!element.getTagName().equals(tag))
                            continue;
                        else if (!element.getFirstChild().getNodeValue().equals(childNum))
                            continue;
                        else
                            rowID = row.getRowId();
                    }
                }
            }
        }

        if(rowID != null)
            return rowID;
        else
            return -1;
    }
    static agileItem getManufacturerPart(String object, Label status) {
        agileItem result = new agileItem();
        GetObjectRequestType getObjectRequestType = new GetObjectRequestType();
        AgileGetObjectRequest agileGetObjectRequest = new AgileGetObjectRequest();
        agileGetObjectRequest.setClassIdentifier("CustomCode");
        agileGetObjectRequest.setObjectNumber(object);

        AgileDataTableRequestType tableRequests = new AgileDataTableRequestType();
        tableRequests.setTableIdentifier(ItemConstants.TABLE_MANUFACTURERS.toString());
        tableRequests.setLoadCellMetaData(false);
        agileGetObjectRequest.getTableRequests().add(tableRequests);
        getObjectRequestType.getRequests().add(agileGetObjectRequest);

        GetObjectResponseType getObjectResponseType = agileBusinessObjectStub.getObject(getObjectRequestType);

        if (getObjectResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS)) {
            status.setText("CustomCode" + " - " + customCode.itemText + " Search  SUCCESS");
            status.getStyleClass().add("label-success");
            AgileGetObjectResponse response = getObjectResponseType.getResponses().get(0);
            AgileObjectType agileObject = response.getAgileObject();
            AgileTableType table = agileObject.getTable().get(0);
            AgileRowType[] rows = table.getRow().toArray(new AgileRowType[0]);

            for (AgileRowType row : rows) {
                result.objectNumber = row.getObjectReferentId().getObjectId().toString();
                List<Element> messages = row.getAny();
                for (Element e : messages)
                    if (e.getTagName().equals("mfrPartNumber"))
                        result.itemText = e.getTextContent();
            }
            return result;

        }
        else {
            status.setText("CustomCode Manufacturer " + object + " Search  FAILURE " +
                    getObjectResponseType.getExceptions().get(0).getException().get(0).getMessage());
            status.getStyleClass().add("label-failure");
        }
        return null;
    }
    static String getNextAutoNumber(String cls) {

        GetAutoNumbersRequestType getAutoNumbersRequestType = new GetAutoNumbersRequestType();
        AgileGetAutoNumbersRequestType agileGetAutoNumbersRequestType = new AgileGetAutoNumbersRequestType();

        agileGetAutoNumbersRequestType.setClassIdentifier(cls);
        agileGetAutoNumbersRequestType.setIncludeAllAutoNumberSource(true);
        agileGetAutoNumbersRequestType.setSize(1);
        getAutoNumbersRequestType.getRequests().add(agileGetAutoNumbersRequestType);

        GetAutoNumbersResponseType getAutoNumbersResponseType = agileAdminStub.getAutoNumbers(getAutoNumbersRequestType);

        if( getAutoNumbersResponseType.getStatusCode().equals(ResponseStatusCode.SUCCESS) ) {
            AgileGetAutoNumbersResponseType[] responses =
                    getAutoNumbersResponseType.getAutoNumberResponses().toArray(new AgileGetAutoNumbersResponseType[0]);
            for (AgileGetAutoNumbersResponseType rs : responses) {
                AutoNumberType[] autonumbers = rs.getAutoNumbers().toArray(new AutoNumberType[0]);
                for (AutoNumberType auto : autonumbers )
                    return auto.getAutoNumber().get(0);
            }
            return null;
        }
        else
            return null;
    }
    static Integer getWorkFlow(String cls, String changeNumber, Label status) {
        GetWorkflowsRequestType getWorkflowsRequestType = new GetWorkflowsRequestType();
        AgileGetWorkflowsRequestType agileGetWorkflowsRequestType = new AgileGetWorkflowsRequestType();
        agileGetWorkflowsRequestType.setClassIdentifier(cls);
        agileGetWorkflowsRequestType.setObjectNumber(changeNumber);
        getWorkflowsRequestType.getWorkflowRequest().add(agileGetWorkflowsRequestType);
        GetWorkflowsResponseType getWorkflowsResponseType = agileCollaborationStub.getWorkFlows(getWorkflowsRequestType);

        if( getWorkflowsResponseType.getStatusCode().equals( ResponseStatusCode.SUCCESS ) ) {
            AgileGetWorkflowsResponseType response = getWorkflowsResponseType.getWorkflowResponse().get(0);
            AgileWorkflowType workflow = response.getWorkflow().get(0);
            return workflow.getWorkflowId();
        }
        else {
            status.setText("getWorkflow FAILURE - " +
                    getWorkflowsResponseType.getExceptions().get(0).getException().get(0).getMessage());
            status.getStyleClass().add("label-failure");
            return -1;
        }
    }
    static Element createTextElement (String tagName, String input) {
        Element element = createMessageElement(tagName);
        element.setTextContent(input.toUpperCase());
        return element;
    }
    static Element createTextElement (String tagName, String input, String attribute) {
        Element element = createMessageElement(tagName);
        element.setTextContent(input.toUpperCase());
        element.setAttribute("attributeId", attribute);
        return element;
    }


    static Element createTextElement(String tagName, ObjectReferentIdType objId) {
        Element element = createMessageElement(tagName, objId);
        element.setAttributeNS(COMMONNAMESPACEURI, "type", "ObjectReferentIdType");
        return element;
    }


    static Element createTextElement (String tagName, double cost, String attribute) {
        Element element = createTextElement(tagName, String.valueOf(cost));
        element.setAttribute("attributeId", attribute);
        return element;
    }

    static Element createTextElement (String tagName, AgileMoneyType cost, String attribute) {
        Element element = createMessageElement(tagName, cost);
        element.setAttribute("attributeId", attribute);
        return element;
    }

    static Element createTextElement (String tagName, TextField inputField) {
        Element element = createMessageElement(tagName);
        element.setTextContent(inputField.getText().toUpperCase());
        return element;
    }

    static Element createTextElement (String tagName, ChoiceBox<String> choiceBox) {
        Element element = createMessageElement(tagName);
        element.setTextContent(choiceBox.getSelectionModel().getSelectedItem());
        return element;
    }


    /*  Create message element for a text field using tagName */

    static Element createMessageElement(String tagName) {
        Document document;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            document = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            return null;
        }
        return document.createElement(tagName);
    }

    static Element createListElement (String tagName, ChoiceBox<String> choiceBox) {
        AgileListEntryType list = new AgileListEntryType();
        SelectionType selection = new SelectionType();
        selection.setValue(choiceBox.getSelectionModel().getSelectedItem());
        list.getSelection().add(selection);
        return createMessageElement(tagName, list);
    }

    static Element createListElement (String tagName, ChoiceBox<String> choiceBox, String attribute) {
        AgileListEntryType list = new AgileListEntryType();
        SelectionType selection = new SelectionType();
        selection.setValue(choiceBox.getSelectionModel().getSelectedItem());
        list.getSelection().add(selection);
        Element element = createMessageElement(tagName, list);
        element.setAttribute("attributeId", attribute);
        return element;
    }

    static Element createListElement (String tagName, String choice, String attribute) {
        AgileListEntryType list = new AgileListEntryType();
        SelectionType selection = new SelectionType();
        selection.setValue(choice);
        list.getSelection().add(selection);
        Element element = createMessageElement(tagName, list);
        element.setAttribute("attributeId", attribute);
        return element;
    }

    /*  Create message element for a List field */

    static  Element createMessageElement(String tagName, AgileListEntryType agileListEntryType) {

        Element element = createMessageElement(tagName);
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(AgileListEntryType.class);
            Marshaller marshaller = jc.createMarshaller();

            JAXBElement<AgileListEntryType> jaxbEl = new JAXBElement(new QName("",element.getNodeName()),
                    AgileListEntryType.class, agileListEntryType);
            marshaller.marshal(jaxbEl, element);
            element =  (Element) element.getFirstChild();

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        element.setAttribute("xmlns:xsi","http://xmlns.oracle.com/AgileObjects/Core/Common/V1");
        element.setAttribute("xsi:type", "AgileListEntryType");
        return element;
    }

    static Element createMessageElement(String tagName, ObjectReferentIdType objectReferentIdType) {

        Element element = createMessageElement(tagName);
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(ObjectReferentIdType.class);
            Marshaller marshaller = jc.createMarshaller();

            JAXBElement<ObjectReferentIdType> jaxbEl = new JAXBElement<>(new QName("",element.getNodeName()),
                    ObjectReferentIdType.class, objectReferentIdType);
            marshaller.marshal(jaxbEl, element);
            element =  (Element) element.getFirstChild();

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        element.setAttribute("xmlns:xsi","http://xmlns.oracle.com/AgileObjects/Core/Common/V1");
        element.setAttribute("xsi:type", "ObjectReferentIdType");
        return element;
    }

    /*  Create message element for a Money field */

    static Element createMessageElement(String tagName, AgileMoneyType money) {

        Element element = createMessageElement(tagName);
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(AgileMoneyType.class);
            Marshaller marshaller = jc.createMarshaller();

            JAXBElement<AgileMoneyType> jaxbEl = new JAXBElement<>(new QName("",element.getNodeName()),
                    AgileMoneyType.class, money);
            marshaller.marshal(jaxbEl, element);
            element =  (Element) element.getFirstChild();
            element.setAttribute("xmlns:xsi","http://xmlns.oracle.com/AgileObjects/Core/Common/V1");
            element.setAttribute("xsi:type", "AgileMoneyType");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return element;
    }

    @Nullable
    public static String getMessageElementValue( Element element) {
        SelectionType selection;
        if (element.getFirstChild() != null) {

            if (element.getAttribute("xsi:type").contains("AgileListEntryType")) {
                AgileListEntryType list = (AgileListEntryType) unmarshalToAgileListEntryType(element);
                if (list != null) {
                    if (list.getSelection().size() != 0) {
                        selection = list.getSelection().get(0);
                        return selection.getValue();
                    }
                }
            } else if (element.getAttribute("xsi:type").contains("AgileMoneyType")) {
                AgileMoneyType money = (AgileMoneyType) unmarshalToAgileMoneyType(element);
                if (money != null) {
                    return money.getAmount().toString();
                }
            } else
                return element.getFirstChild().getNodeValue();
        }
        return null;
    }

    public static Object unmarshalToAgileListEntryType ( Node node) {

        try {
            JAXBContext jc = null;
            Class<?> clz = null;

            String xsiType = ((Element)node).getAttributeNS("http://xmlns.oracle.com/AgileObjects/Core/Common/V1", "type");
            boolean isXSITypeEmpty = "".equals(xsiType) || null==xsiType ? true : false;

            jc = JAXBContext.newInstance(AgileListEntryType.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(node);
            JAXBElement<?> jaxbEle = (JAXBElement<?>) obj;

            return jaxbEle.getValue();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object unmarshalToAgileMoneyType (Node node) {

        try {
            JAXBContext jc = null;
            Class<?> clz = null;

            String xsiType = ((Element)node).getAttributeNS("http://xmlns.oracle.com/AgileObjects/Core/Common/V1", "type");
            boolean isXSITypeEmpty = "".equals(xsiType) || null==xsiType ? true : false;

            jc = JAXBContext.newInstance(AgileMoneyType.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(node);
            JAXBElement<?> jaxbEle = (JAXBElement<?>) obj;

            return jaxbEle.getValue();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
