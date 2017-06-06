package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.model.role.Roles;

import java.util.ArrayList;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class DaytimeStateRoom extends AbstractStateRoom {
    public DaytimeStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case DAYTIME_VOTING:
                resolveDaytimeVoting(event);
                break;
            case WEREWOLVES_EXPLODE:
                resolveWereWolfExplode(event);
                break;
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

    private void resolveWereWolfExplode(JudgeEvent event) {
        Integer seatNumber = event.getExplodeWereWolf();
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(seatNumber);
        if (seatInfo.isAlive() && Roles.WEREWOLVES.equals(seatInfo.getRole())) {
            roomState.getLastDaytimeRecord().setDiedNumber(seatNumber);
            seatInfo.setAlive(false);
            resolveNightComing();
        }else{
            throw new RuntimeException("该角色无法自爆");
        }
    }


    /**
     * 开始投票事件
     *
     * @param event
     */
    private void resolveDaytimeVoting(JudgeEvent event) {
        //开始投票
        this.roomState.setStatus(RoomStatus.VOTING);
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        displayInfo.addAcceptableEventType(JudgeEventType.DAYTIME_VOTING);
        //白天狼人自爆,由法官操作
        displayInfo.addAcceptableEventType(JudgeEventType.WEREWOLVES_EXPLODE);
        //白天阶段可以结束游戏
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
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        return displayInfo;
    }
}
