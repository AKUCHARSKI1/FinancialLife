package com.example.apka_inzynierka;

import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private int transactionId;
    private String transactionName;
    private Double transactionAmount;
    private String username;
    private String transactionCategory;
    private String transactionDescription;
    private Date transactionDate;
    private int targetId;
    private int obligationId;

    private String state;

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public Transaction(int transactionId, String transactionName, Double transactionAmount, String username, String transactionCategory, String transactionDescription, Date transactionDate, int targetId, int obligationId, String state) {
        this.transactionId = transactionId;
        this.transactionName = transactionName;
        this.transactionAmount = transactionAmount;
        this.username = username;
        this.transactionCategory = transactionCategory;
        this.transactionDescription = transactionDescription;
        this.transactionDate = transactionDate;
        this.targetId = targetId;
        this.obligationId = obligationId;
        this.state = state;
    }


    public String getState() {

        if(state == null) state = "Aktywny";
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTransactionCategory() {
        return transactionCategory;
    }

    public void setTransactionCategory(String transactionCategory) {
        this.transactionCategory = transactionCategory;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getObligationId() {
        return obligationId;
    }

    public void setObligationId(int obligationId) {
        this.obligationId = obligationId;
    }


}
