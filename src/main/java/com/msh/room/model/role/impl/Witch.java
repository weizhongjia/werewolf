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
public class Witch extends AssignedPlayer {
    private boolean alive;

    public Witch(RoomStateData roomState, int number) {
        super(roomState, number);
        alive = roomState.getPlaySeatInfoBySeatNumber(number).isAlive();
    }

    @Override
    public void calculateScore() {
        //TODO 女巫结算
        if (GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())) {
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
            seatInfo.setFinalScore(5);
        }
    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        if (alive) {
            if (RoomStatus.NIGHT.equals(roomState.getStatus())) {
                NightRecord nightRecord = roomState.getLastNightRecord();
                PlayerEventType eventType = getWitchNightEvent(nightRecord);
                if (eventType != null) {
                    displayInfo.addAcceptableEventType(eventType);
                }
            }
        }
        return displayInfo;
    }

    public PlayerEventType getWitchNightEvent(NightRecord nightRecord) {
        Integer wolfKilledSeat = nightRecord.getWolfKilledSeat();
        PlayerSeatInfo wolfKillInfo = roomState.getPlaySeatInfoBySeatNumber(wolfKilledSeat);
        //女巫用药询问逻辑,此处逻辑仅限不能同时用两种药的情况.逻辑处理不太好，需要再做封装
        if (wolfKilledSeat == null) {
            return null;
        }
        //女巫本轮未询问用解药==null，女巫解药可用，狼人没有杀女巫: WITCH_SAVE
        if (nightRecord.getWitchSaved() == null && roomState.getWitchState().isAntidoteAvailable() && !Roles.WITCH.equals(wolfKillInfo.getRole())) {
            return PlayerEventType.WITCH_SAVE;
        }
        //女巫本轮未询问用解药==null，(女巫解药不可用 或者 狼杀了女巫) FAKE_WITCH_SAVE
        else if (nightRecord.getWitchSaved() == null && (!roomState.getWitchState().isAntidoteAvailable() || Roles.WITCH.equals(wolfKillInfo.getRole()))) {
            return PlayerEventType.FAKE_WITCH_SAVE;
        }
        //女巫本轮已询问用解药,但未用==0，但未询问用毒药==null，女巫毒药可用 WITCH_POISON
        else if (nightRecord.getWitchSaved() == 0 && nightRecord.getWitchPoisoned() == null
                && roomState.getWitchState().isPoisonAvailable()) {
            return PlayerEventType.WITCH_POISON;
        }
        //女巫本轮已询问用解药,但未用==0，但未询问用毒药==null，女巫毒药不可用 FAKE_WITCH_POISON
        else if (nightRecord.getWitchSaved() == 0 && nightRecord.getWitchPoisoned() == null
                && !roomState.getWitchState().isPoisonAvailable()) {
            return PlayerEventType.FAKE_WITCH_POISON;
        }
        //女巫本轮已询问用解药，但已用!=0，但未询问是否用毒药 FAKE_WITCH_POISON
        else if (nightRecord.getWitchSaved() != 0 && nightRecord.getWitchPoisoned() == null) {
            return PlayerEventType.FAKE_WITCH_POISON;
        }
        return null;
    }

    public void save(boolean witchSave) {
        if (roomState.getWitchState().isAntidoteAvailable()) {
            Integer killedSeat = roomState.getLastNightRecord().getWolfKilledSeat();
            if (killedSeat.equals(this.number)) {
                throw new RoomBusinessException("女巫无法自救");
            }
            //空刀时相当于没救
            if (witchSave && killedSeat != 0) {
                roomState.getLastNightRecord().setWitchSaved(killedSeat);
                roomState.getWitchState().setAntidoteAvailable(false);
            } else {
                roomState.getLastNightRecord().setWitchSaved(0);
            }
        } else {
            throw new RoomBusinessException("女巫无法使用解药");
        }
    }


    public void poison(Integer witchPoisonNumber) {
        if (roomState.getWitchState().isPoisonAvailable()) {
            roomState.getLastNightRecord().setWitchPoisoned(witchPoisonNumber);
        } else {
            throw new RoomBusinessException("女巫无法使用毒药");
        }
    }

    public void fakePoison() {
        roomState.getLastNightRecord().setWitchPoisoned(0);
    }

    public void fakeSave() {
        roomState.getLastNightRecord().setWitchSaved(0);
    }
}
