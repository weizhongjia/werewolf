package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Witch extends CommonPlayer {
    private boolean alive;

    public Witch(RoomStateData roomState, int number) {
        super(roomState, number);
        alive = roomState.getPlaySeatInfoBySeatNumber(number).isAlive();
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
        List<PlayerSeatInfo> playerSeatInfos = PlayerRoleMask.maskPlayerRole(roomState.getPlayerSeatInfo(), Arrays.asList(number));
        displayInfo.setPlayerSeatInfoList(playerSeatInfos);
        displayInfo.setAcceptableEventTypeList(new ArrayList<>());
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
        if (nightRecord.getWitchSaved() == null && roomState.getWitchState().isAntidoteAvailable()) {
            return PlayerEventType.WITCH_SAVE;
        }
        if (nightRecord.getWitchSaved() == 0 && nightRecord.getWitchPoisoned() == null
                && roomState.getWitchState().isPoisonAvailable()) {
            return PlayerEventType.WITCH_POISON;
        }
        return null;
    }
}
