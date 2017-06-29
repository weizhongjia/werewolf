package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.List;
import java.util.Map;
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
        int initialScore = 5;
        if (GameResult.WEREWOLVES_WIN.equals(roomState.getGameResult())) {
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
            int finalScore = calculateFinalScore(initialScore);
            if (finalScore > 17) {
                finalScore = 17;
            }
            seatInfo.setFinalScore(finalScore);
        }
    }

    private int calculateFinalScore(int initialScore) {
        int finalScore = initialScore;
        //貌似白天晚上永远一样，因为只能天亮了才能结算Over。
        int nightSize = roomState.getNightRecordList().size();
        int dayTimeSize = roomState.getDaytimeRecordList().size();
        if (dayTimeSize < 3) {
            finalScore = 17;
        }
        if (dayTimeSize == 3) {
            finalScore = 12;
        }
        if (dayTimeSize == 4) {
            finalScore = 7;
        }
        if (dayTimeSize == 5) {
            finalScore = 6;
        }
        if (dayTimeSize >= 6) {
            finalScore = 3;
        }
        return finalScore;
    }

    public void explode() {
        roomState.getLastDaytimeRecord().setDiedNumber(number);
        roomState.getLastDaytimeRecord().setWolfExplode(number);
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        gameEndingCalculate();
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
