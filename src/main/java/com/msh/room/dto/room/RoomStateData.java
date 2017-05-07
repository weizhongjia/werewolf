package com.msh.room.dto.room;

import com.msh.room.dto.response.seat.PlayerSeatInfo;
import com.msh.room.model.role.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class RoomStateData {
    private String roomCode;
    private RoomStatus status;
    private List<PlayerSeatInfo> playerSeatInfo;
    private Map<Roles, Integer> gameConfig;


    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public List<PlayerSeatInfo> getPlayerSeatInfo() {
        return playerSeatInfo;
    }

    public void setPlayerSeatInfo(List<PlayerSeatInfo> playerSeatInfo) {
        this.playerSeatInfo = playerSeatInfo;
    }

    public void addPlaySeatInfo(PlayerSeatInfo seatInfo) {
        if (playerSeatInfo == null) {
            playerSeatInfo = new ArrayList<>();
        }
        playerSeatInfo.add(seatInfo);
    }

    public Map<Roles, Integer> getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(Map<Roles, Integer> gameConfig) {
        this.gameConfig = gameConfig;
    }
}
