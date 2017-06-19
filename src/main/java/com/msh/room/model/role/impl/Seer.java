package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.Roles;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Seer extends AssignedPlayer {
    private boolean alive;

    public Seer(RoomStateData roomState, int number) {
        super(roomState, number);
        this.alive = roomState.getPlaySeatInfoBySeatNumber(number).isAlive();
    }

    @Override
    public void calculateScore() {
        //TODO 预言家结算
        if (GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())) {
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
            seatInfo.setFinalScore(5);
        }
    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);

        //预言家要看到曾经验到的人
        if (roomState.getNightRecordList() != null
                && !RoomStatus.GAME_OVER.equals(roomState.getStatus())) {
            roomState.getNightRecordList().stream().forEach(
                    nightRecord -> {
                        Integer seerVerify = nightRecord.getSeerVerify();
                        if (seerVerify != null) {
                            if (Roles.WEREWOLVES.equals(roomState.getPlaySeatInfoBySeatNumber(seerVerify).getRole())) {
                                displayInfo.getPlayerSeatInfoList().get(seerVerify - 1).setRole(Roles.WEREWOLVES);
                            } else {
                                displayInfo.getPlayerSeatInfoList().get(seerVerify - 1).setRole(Roles.VILLAGER);
                            }
                        }
                    }
            );
        }
        if (isVerifyEnable()) {
            displayInfo.addAcceptableEventType(PlayerEventType.SEER_VERIFY);
        }
        return displayInfo;
    }

    public boolean isVerifyEnable() {
        if (alive) {
            if (RoomStatus.NIGHT.equals(roomState.getStatus()) && roomState.getLastNightRecord().getSeerVerify() == null) {
                return true;
            }
        }
        return false;
    }

    public void verify(Integer seerVerifyNumber) {
        if (isVerifyEnable()) {
            NightRecord lastNightRecord = roomState.getLastNightRecord();
            lastNightRecord.setSeerVerify(seerVerifyNumber);

            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(seerVerifyNumber);
            lastNightRecord.setSeerVerifyResult(Roles.WEREWOLVES.equals(seatInfo.getRole()));
        } else throw new RoomBusinessException("目前预言家无法验人");
    }

    public void fakeVerify() {
        NightRecord lastNightRecord = roomState.getLastNightRecord();
        lastNightRecord.setSeerVerify(0);
        lastNightRecord.setSeerVerifyResult(false);
    }
}

