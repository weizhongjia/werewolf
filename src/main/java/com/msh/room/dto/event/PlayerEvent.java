package com.msh.room.dto.event;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class PlayerEvent {
    private int seatNumber;
    private String userID;

    public PlayerEvent() {
    }

    public PlayerEvent(int seatNumber, String userID) {
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
}
