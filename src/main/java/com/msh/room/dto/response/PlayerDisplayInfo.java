package com.msh.room.dto.response;

import com.msh.room.dto.response.seat.PlayerSeatInfo;

import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class PlayerDisplayInfo {
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
}
