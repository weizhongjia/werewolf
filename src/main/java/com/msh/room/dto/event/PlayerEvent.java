package com.msh.room.dto.event;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class PlayerEvent {
    private PlayerEventType eventType;
    private int seatNumber;
    private String userID;

    //狼人杀人对象(0为空刀)
    private Integer wolfKillNumber;

    //true是救人，false是不救
    private boolean witchSave;
    //女巫毒人号码
    private Integer witchPoisonNumber;


    private Integer daytimeVoteNumber;
    private Integer pkVoteNumber;

    public PlayerEvent() {
    }

    public PlayerEvent(PlayerEventType eventType, int seatNumber, String userID) {
        this.eventType = eventType;
        this.seatNumber = seatNumber;
        this.userID = userID;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public PlayerEventType getEventType() {
        return eventType;
    }

    public void setEventType(PlayerEventType eventType) {
        this.eventType = eventType;
    }

    public Integer getWolfKillNumber() {
        return wolfKillNumber;
    }

    public void setWolfKillNumber(Integer wolfKillNumber) {
        this.wolfKillNumber = wolfKillNumber;
    }

    public boolean isWitchSave() {
        return witchSave;
    }

    public void setWitchSave(boolean witchSave) {
        this.witchSave = witchSave;
    }

    public Integer getWitchPoisonNumber() {
        return witchPoisonNumber;
    }

    public void setWitchPoisonNumber(Integer witchPoisonNumber) {
        this.witchPoisonNumber = witchPoisonNumber;
    }

    public Integer getDaytimeVoteNumber() {
        return daytimeVoteNumber;
    }

    public void setDaytimeVoteNumber(Integer daytimeVoteNumber) {
        this.daytimeVoteNumber = daytimeVoteNumber;
    }

    public Integer getPkVoteNumber() {
        return pkVoteNumber;
    }

    public void setPkVoteNumber(Integer pkVoteNumber) {
        this.pkVoteNumber = pkVoteNumber;
    }
}
