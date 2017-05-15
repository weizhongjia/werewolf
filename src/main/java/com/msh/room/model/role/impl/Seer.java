package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Seer extends CommonPlayer {
    private boolean alive;

    public Seer(RoomStateData roomState, int number) {
        super(roomState, number);
        this.alive = roomState.getPlaySeatInfoBySeatNumber(number).isAlive();
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
        if(isVerifyEnable()){
            NightRecord lastNightRecord = roomState.getLastNightRecord();
            lastNightRecord.setSeerVerify(seerVerifyNumber);

            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(seerVerifyNumber);
            lastNightRecord.setSeerVerifyResult(Roles.WEREWOLVES.equals(seatInfo.getRole()));
        }else throw new RoomBusinessException("目前预言家无法验人");
    }
}
