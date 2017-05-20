package com.msh.room.model.role.impl;

import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/18.
 */
public abstract class AssignedPlayer extends CommonPlayer {
    public AssignedPlayer(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData killed() {
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        return roomState;
    }

    public void resolveCommonDisplayInfo(PlayerDisplayInfo displayInfo) {
        //注入昨夜信息
        NightRecord lastNightRecord = this.roomState.getLastNightRecord();
        displayInfo.setNightRecord(lastNightRecord);
        //除自己以外的玩家均覆盖身份
        displayInfo.setPlayerInfo(roomState.getPlayerSeatInfo().get(number - 1));
        List<PlayerSeatInfo> playerSeatInfos = PlayerRoleMask.maskPlayerRole(roomState.getPlayerSeatInfo(), Arrays.asList(number));
        displayInfo.setPlayerSeatInfoList(playerSeatInfos);
        displayInfo.setAcceptableEventTypeList(new ArrayList<>());
    }
}
