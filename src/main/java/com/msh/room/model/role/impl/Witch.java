package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.response.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Witch extends CommonPlayer{
    public Witch(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData killed() {
        return null;
    }

    @Override
    public RoomStateData vote() {
        return null;
    }

    @Override
    public RoomStateData resolveEvent(PlayerEvent event) {
        return null;
    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        roomState.getPlayerSeatInfo().stream().filter(seatInfo -> seatInfo.getSeatNumber() == number).forEach(seatInfo -> {
            displayInfo.setPlayerInfo(seatInfo);
        });
        List<PlayerSeatInfo> playerSeatInfos = PlayerRoleMask.maskPlayerRole(roomState.getPlayerSeatInfo(), Arrays.asList(number));
        displayInfo.setPlayerSeatInfoList(playerSeatInfos);
        return displayInfo;
    }
}
