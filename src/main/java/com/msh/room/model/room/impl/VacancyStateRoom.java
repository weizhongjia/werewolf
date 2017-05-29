package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 空闲房间
 * Created by zhangruiqian on 2017/5/25.
 */
public class VacancyStateRoom extends AbstractStateRoom {
    public VacancyStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case CREATE_ROOM:
                resolveCreateRoomEvent(event);
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

    private void resolveCreateRoomEvent(JudgeEvent event) {
        roomState.setStatus(RoomStatus.CRATING);
        roomState.setGameConfig(event.getGameConfig());
        //初始化座位
        roomState.setPlayerSeatInfo(initSeatInfo(event.getGameConfig()));
    }

    private List<PlayerSeatInfo> initSeatInfo(Map<Roles, Integer> gameConfig) {
        List playerInfoList = new ArrayList();
        int seatNumber = 1;
        for (Roles role : gameConfig.keySet()) {
            Integer num = gameConfig.get(role);
            for (int i = 0; i < num; i++) {
                PlayerSeatInfo seatInfo = new PlayerSeatInfo(seatNumber, true);
                seatInfo.setAlive(false);
                seatInfo.setRole(Roles.NONE);
                playerInfoList.add(seatInfo);
                seatNumber++;
            }
        }
        return playerInfoList;
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        displayInfo.addAcceptableEventType(JudgeEventType.CREATE_ROOM);
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
        throw new RoomBusinessException("房间尚未创建，无法获取信息");
    }

}
