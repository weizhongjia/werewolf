package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.response.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.Roles;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class Werewolves extends CommonPlayer {
    public Werewolves(RoomStateData state, int number) {
        super(state, number);
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
        setOtherPlayersInfo(displayInfo);
        return displayInfo;
    }

    private PlayerDisplayInfo setOtherPlayersInfo(PlayerDisplayInfo displayInfo) {
        roomState.getPlayerSeatInfo().stream().forEach(seatInfo -> {
            PlayerSeatInfo playerSeatInfo = new PlayerSeatInfo(seatInfo.getSeatNumber(), false);
            playerSeatInfo.setAlive(seatInfo.isAlive());
            if (Roles.WEREWOLVES == seatInfo.getRole()) {
                playerSeatInfo.setRole(seatInfo.getRole());
            } else {
                playerSeatInfo.setRole(null);
            }
            displayInfo.addPlayerSeatInfo(playerSeatInfo);
        });
        return displayInfo;
    }
}
