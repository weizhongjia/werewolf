package com.msh.room.dto.room.state;

import com.msh.room.dto.room.RoomStatus;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
public class HunterState {
    private RoomStatus nextStatus;
    private Integer shootNumber;


    public RoomStatus getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(RoomStatus nextStatus) {
        this.nextStatus = nextStatus;
    }

    public Integer getShootNumber() {
        return shootNumber;
    }

    public void setShootNumber(Integer shootNumber) {
        this.shootNumber = shootNumber;
    }
}
