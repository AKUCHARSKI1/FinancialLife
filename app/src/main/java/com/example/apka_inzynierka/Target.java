package com.example.apka_inzynierka;

import java.io.Serializable;
import java.util.Date;

public class Target implements Serializable {

    private int targetId;
    private String targetName;
    private String username;
    private Double targetAmount;
    private Double targetActualAmount;
    private String targetState;
    private Date targetEndDate;
    private Date targetUpdateDate;

    public Target(int targetId, String targetName, String username, Double targetAmount, Double targetActualAmount, String targetState, Date targetEndDate, Date targetUpdateDate) {
        this.targetId = targetId;
        this.targetName = targetName;
        this.username = username;
        this.targetAmount = targetAmount;
        this.targetActualAmount = targetActualAmount;
        this.targetState = targetState;
        this.targetEndDate = targetEndDate;
        this.targetUpdateDate = targetUpdateDate;
    }

    public Double getTargetActualAmount() {
        return targetActualAmount;
    }

    public void setTargetActualAmount(Double targetActualAmount) {
        this.targetActualAmount = targetActualAmount;
    }
    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getTargetState() {
        return targetState;
    }

    public void setTargetState(String targetState) {
        this.targetState = targetState;
    }

    public Date getTargetEndDate() {
        return targetEndDate;
    }

    public void setTargetEndDate(Date targetEndDate) {
        this.targetEndDate = targetEndDate;
    }

    public Date getTargetUpdateDate() {
        return targetUpdateDate;
    }

    public void setTargetUpdateDate(Date targetUpdateDate) {
        this.targetUpdateDate = targetUpdateDate;
    }
}
