package com.db;

import javafx.beans.property.SimpleStringProperty;


public class exDisplay {

    private final SimpleStringProperty exOpp;
    private final SimpleStringProperty exName;
    private final SimpleStringProperty exDate;
    private final SimpleStringProperty exStatus;
    private final SimpleStringProperty exNumber;
    private final SimpleStringProperty exAgileId;
    private final SimpleStringProperty exECO_MCO;
    private final SimpleStringProperty exApprovers;
    private final SimpleStringProperty exPrelim;
    private final SimpleStringProperty exFA;
    private final SimpleStringProperty exRevise;
    private final SimpleStringProperty exCodes;

    exDisplay(String opp, String name, String dueDate, String status, String number, String agileId, String change,
              String approvers, String prelim, String firstArticle, String revise, String codes) {
        this.exOpp = new SimpleStringProperty(opp);
        this.exName = new SimpleStringProperty(name);
        this.exDate = new SimpleStringProperty(dueDate);
        this.exStatus = new SimpleStringProperty(status);
        this.exNumber = new SimpleStringProperty(number);
        this.exAgileId = new SimpleStringProperty(agileId);
        this.exECO_MCO = new SimpleStringProperty(change);
        this.exApprovers = new SimpleStringProperty(approvers);
        this.exPrelim = new SimpleStringProperty(prelim);
        this.exRevise = new SimpleStringProperty(revise);
        this.exFA = new SimpleStringProperty(firstArticle);
        this.exCodes = new SimpleStringProperty(codes);

    }
    public String getExOpp () {return exOpp.get();}
    public String getExName () {return exName.get();}
    public String getExDate () {return exDate.get();}
    public String getExStatus () {return exStatus.get();}
    public String getExNumber () {return exNumber.get();}
    public String getExAgileId () {return exAgileId.get();}
    public String getExECO_MCO () {return exECO_MCO.get();}
    public String getExApprovers () {return exApprovers.get();}
    public String getExPrelim () {return exPrelim.get();}
    public String getExFA () {return exFA.get();}
    public String getExRevise () {return exRevise.get();}
    public String getExCodes () {return exCodes.get();}

    public void setExPrelim(String prelim) {exPrelim.set(prelim);}
    public void setExFA(String firstArticle) {exFA.set(firstArticle);}
    public void setExECO_MCO(String change) {exECO_MCO.set(change);}
    public void setExCodes(String codes) {exCodes.set(codes);}
    public void setExRevise(String codes) {exRevise.set(codes);}

}
