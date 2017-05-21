package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.Roles;

/**
 * 未分配角色玩家
 * Created by zhangruiqian on 2017/5/7.
 */
public class UnAssignPlayer extends CommonPlayer {
    public UnAssignPlayer(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData killed() {
        return null;
    }

    @Override
    public RoomStateData resolveEvent(PlayerEvent event) {
        if (PlayerEventType.Exit_ROOM.equals(event.getEventType())) {
            PlayerSeatInfo seatInfo = roomState.getPlayerSeatInfo().get(event.getSeatNumber() - 1);
            seatInfo.setRole(Roles.NONE);
            seatInfo.setUserID(null);
            seatInfo.setAlive(false);
            seatInfo.setSeatAvailable(true);
        }
        return roomState;
    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        roomState.getPlayerSeatInfo().stream().filter(seatInfo -> seatInfo.getSeatNumber() == number).forEach(seatInfo -> {
            displayInfo.setPlayerInfo(seatInfo);
        });
        displayInfo.setPlayerSeatInfoList(roomState.getPlayerSeatInfo());
        displayInfo.addAcceptableEventType(PlayerEventType.Exit_ROOM);
        return displayInfo;
    }
}
