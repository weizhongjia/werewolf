package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class Werewolves extends AssignedPlayer {
    private boolean alive;

    public Werewolves(RoomStateData state, int number) {
        super(state, number);
        this.alive = state.getPlaySeatInfoBySeatNumber(number).isAlive();
    }

    @Override
    public void calculateScore() {
        //TODO 狼人结算
        int initialScore = 6;
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
        if (GameResult.WEREWOLVES_WIN.equals(roomState.getGameResult())) {
            seatInfo.setFinalScore(17);
        }
    }


    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        setOtherPlayersInfo(displayInfo);

        if (alive) {
            if (RoomStatus.NIGHT.equals(roomState.getStatus()) && roomState.getLastNightRecord().getWolfKilledSeat() == null) {
                displayInfo.addAcceptableEventType(PlayerEventType.WOLF_KILL);
            }
            //仅发言阶段
            if (RoomStatus.DAYTIME.equals(roomState.getStatus())
                    || RoomStatus.PK.equals(roomState.getStatus())
                    || RoomStatus.SHERIFF_RUNNING.equals(roomState.getStatus())
                    || RoomStatus.SHERIFF_PK.equals(roomState.getStatus())) {
                //TODO 狼人自爆
            }
        }
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
