package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.impl.Hunter;

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
        filterPlayerEventType(event);
        switch (event.getEventType()) {
            case HUNTER_SHOOT:
                hunterShoot(event);
                break;
        }
        return roomState;
    }

    private RoomStateData hunterShoot(PlayerEvent event) {
        if (!Roles.HUNTER.equals(roomState.getPlaySeatInfoBySeatNumber(event.getSeatNumber()).getRole())) {
            throw new RoomBusinessException("你不是猎人无法开枪");
        }
        Hunter hunter = (Hunter) PlayerRoleFactory.createPlayerInstance(roomState, event.getSeatNumber());
        return hunter.shoot(event.getShootNumber());
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        //进入猎人时间后，可以放开白天信息。猎人投票死亡需要公布票型，猎人夜晚死亡白天为空信息
        displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        return displayInfo;
    }
}
