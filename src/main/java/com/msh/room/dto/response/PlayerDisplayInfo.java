package com.msh.room.dto.response;

import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.room.seat.PlayerSeatInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class PlayerDisplayInfo {
    private List<PlayerEventType> acceptableEventTypeList;

    private PlayerSeatInfo playerInfo;
    private List<PlayerSeatInfo> playerSeatInfoList;

    public PlayerDisplayInfo() {
    }

    public PlayerSeatInfo getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(PlayerSeatInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    public List<PlayerSeatInfo> getPlayerSeatInfoList() {
        return playerSeatInfoList;
    }

    public void setPlayerSeatInfoList(List<PlayerSeatInfo> playerSeatInfoList) {
        this.playerSeatInfoList = playerSeatInfoList;
    }

    public void addPlayerSeatInfo(PlayerSeatInfo seatInfo) {
        if (playerSeatInfoList == null) {
            playerSeatInfoList = new ArrayList<>();
        }
        playerSeatInfoList.add(seatInfo);
    }

    public List<PlayerEventType> getAcceptableEventTypeList() {
        return acceptableEventTypeList;
    }

    public void setAcceptableEventTypeList(List<PlayerEventType> acceptableEventTypeList) {
        this.acceptableEventTypeList = acceptableEventTypeList;
    }

    public void addAcceptableEventType(PlayerEventType type) {
        if (this.acceptableEventTypeList == null) {
            this.acceptableEventTypeList = new ArrayList<>();
        }
        acceptableEventTypeList.add(type);
    }
}
