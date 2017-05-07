package com.msh.room.dto.response;

import com.msh.room.dto.response.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class JudgeDisplayInfo {
    private String roomCode;
    private RoomStatus status;
    private List<PlayerSeatInfo> playerSeatInfo;

    public JudgeDisplayInfo() {
    }

    public JudgeDisplayInfo(String roomCode) {
        this.roomCode = roomCode;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public List<PlayerSeatInfo> getPlayerSeatInfo() {
        return playerSeatInfo;
    }

    public void setPlayerSeatInfo(List<PlayerSeatInfo> playerSeatInfo) {
        this.playerSeatInfo = playerSeatInfo;
    }

    public void addPlayerSeatInfo(PlayerSeatInfo seatInfo) {
        if (this.playerSeatInfo != null) {
            playerSeatInfo = new ArrayList<>();
        }
        playerSeatInfo.add(seatInfo);
    }
}
