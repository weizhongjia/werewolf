package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.response.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.List;
import java.util.stream.Collectors;

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
        displayInfo.setPlayerInfo(roomState.getPlayerSeatInfo().get(number - 1));
        setOtherPlayersInfo(displayInfo);
        return displayInfo;
    }

    private PlayerDisplayInfo setOtherPlayersInfo(PlayerDisplayInfo displayInfo) {
        List<Integer> werewolvesNumbers = roomState.getPlayerSeatInfo().stream()
                .filter(seatInfo -> Roles.WEREWOLVES.equals(seatInfo.getRole()))
                .map(PlayerSeatInfo::getSeatNumber).collect(Collectors.toList());

        List<PlayerSeatInfo> playerSeatInfos = PlayerRoleMask.maskPlayerRole(roomState.getPlayerSeatInfo(), werewolvesNumbers);
        displayInfo.setPlayerSeatInfoList(playerSeatInfos);
        return displayInfo;
    }
}
