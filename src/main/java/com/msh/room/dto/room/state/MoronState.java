package com.msh.room.dto.room.state;

import com.msh.room.dto.room.RoomStatus;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
public class MoronState {
    private boolean beanVoted;
    private RoomStatus lastRoomStatus;

    public boolean isBeanVoted() {
        return beanVoted;
    }

    public void setBeanVoted(boolean beanVoted) {
        this.beanVoted = beanVoted;
    }

    public RoomStatus getLastRoomStatus() {
        return lastRoomStatus;
    }

    public void setLastRoomStatus(RoomStatus lastRoomStatus) {
        this.lastRoomStatus = lastRoomStatus;
    }
}
