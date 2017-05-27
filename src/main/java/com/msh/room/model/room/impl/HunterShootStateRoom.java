package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;

import java.util.ArrayList;

import static com.msh.room.dto.event.JudgeEventType.DISBAND_GAME;
import static com.msh.room.dto.event.JudgeEventType.GAME_ENDING;
import static com.msh.room.dto.event.JudgeEventType.RESTART_GAME;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class HunterShootStateRoom extends AbstractStateRoom {
    public HunterShootStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case GAME_ENDING:
                resolveGameEnding(event);
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

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        //猎人可以结束游戏
        if (roomState.getGameResult() != null) {
            displayInfo.setAcceptableEventTypes(new ArrayList<>());
            displayInfo.addAcceptableEventType(JudgeEventType.GAME_ENDING);
        }
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        return null;
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        return null;
    }
}
