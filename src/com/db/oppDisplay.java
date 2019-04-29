package com.db;

import javafx.beans.property.SimpleStringProperty;

public class oppDisplay {

    private final SimpleStringProperty oppOpportunity;
    private final SimpleStringProperty oppStatus;
    private final SimpleStringProperty oppDate;
    private final SimpleStringProperty oppQualified;
    private final SimpleStringProperty oppPrimary;
    private final SimpleStringProperty oppException;

    oppDisplay(String opp, String status, String oppDate, String qual, String primary, String exception) {

        this.oppOpportunity = new SimpleStringProperty(opp);
        this.oppStatus = new SimpleStringProperty(status);
        this.oppDate = new SimpleStringProperty(oppDate);
        this.oppQualified = new SimpleStringProperty(qual);
        this.oppPrimary = new SimpleStringProperty(primary);
        this.oppException = new SimpleStringProperty(exception);
    }

    public String getOppOpportunity () {return oppOpportunity.get();}
    public String getOppStatus () {return oppStatus.get();}
    public String getOppDate () {return oppDate.get();}
    public String getOppQualified () {return oppQualified.get();}
    public String getOppPrimary () {return oppPrimary.get();}
    public String getOppException () {return oppException.get();}

    public void setOppPrimary(String prim) {oppPrimary.set(prim);}
    public void setOppStatus(String status) {oppStatus.set(status);}
}

