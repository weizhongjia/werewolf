package com.msh.room.dto.event;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class PlayerEvent {
    private PlayerEventType eventType;
    private int seatNumber;
    private String userID;

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
}
