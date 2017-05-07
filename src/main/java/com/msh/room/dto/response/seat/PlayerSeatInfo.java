package com.msh.room.dto.response.seat;

import com.msh.room.model.role.Roles;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class PlayerSeatInfo {
    private String userID;
    private Roles role;
    private Integer seatNumber;
    private boolean alive;
    //false 有人 true 空闲
    private boolean seatAvailable;


    public PlayerSeatInfo(Integer seatNumber, boolean seatAvailable) {
        this.seatNumber = seatNumber;
        this.seatAvailable = seatAvailable;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isSeatAvailable() {
        return seatAvailable;
    }

    public void setSeatAvailable(boolean seatAvailable) {
        this.seatAvailable = seatAvailable;
    }
}
