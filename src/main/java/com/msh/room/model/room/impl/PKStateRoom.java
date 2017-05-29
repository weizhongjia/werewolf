package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;

import java.util.ArrayList;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class PKStateRoom extends AbstractStateRoom {
    public PKStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case DAYTIME_PK_VOTING:
                resolvePKVoting(event);
                break;
            case RESTART_GAME:
                resolveRestartGameEvent(event);
                break;
            case DISBAND_GAME:
                resolveDisbandGameEvent(event);
                break;
        }
        return roomState;
    }

    private void resolvePKVoting(JudgeEvent event) {
        if (this.roomState.getLastDaytimeRecord().getPkVotingRecord().size() < 2) {
            this.roomState.setStatus(RoomStatus.PK_VOTING);
        }
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        displayInfo.addAcceptableEventType(JudgeEventType.DAYTIME_PK_VOTING);
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        return roomState;
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        //PK环节说明投票结束(无论投票还是pk投票)，也公布白天信息
        displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        return displayInfo;
    }
}
