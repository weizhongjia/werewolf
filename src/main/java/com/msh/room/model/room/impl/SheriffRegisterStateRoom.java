package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class SheriffRegisterStateRoom extends AbstractStateRoom {
    public SheriffRegisterStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case RESTART_GAME:
                resolveRestartGameEvent(event);
                break;
            case DISBAND_GAME:
                resolveDisbandGameEvent(event);
                break;
        }
        return roomState;
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        displayInfo.setSheriffRecord(roomState.getSheriffRecord());
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        switch (event.getEventType()) {
            case SHERIFF_REGISTER:
                playerRegister(event);
                break;
        }
        return roomState;
    }

    private void playerRegister(PlayerEvent event) {
        int seatNumber = event.getSeatNumber();
        if (roomState.getSheriffRecord().getSheriffRegisterList().contains(seatNumber)) {
            throw new RoomBusinessException("您已上警");
        }
        roomState.getSheriffRecord().registSheriff(seatNumber);
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        if (!roomState.getSheriffRecord().getSheriffRegisterList().contains(seatNumber)) {
            displayInfo.addAcceptableEventType(PlayerEventType.SHERIFF_REGISTER);
        } else {
            //可查看上警信息
            displayInfo.setSheriffRecord(roomState.getSheriffRecord());
        }
        return displayInfo;
    }
}
