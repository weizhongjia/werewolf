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
 * Created by zhangruiqian on 2017/6/5.
 */
public class SheriffSwitchTimeStateRoom extends AbstractStateRoom {
    public SheriffSwitchTimeStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case SHERIFF_SWITCH:
                resolveSheriffSwitch(event);
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

    private void resolveSheriffSwitch(JudgeEvent event) {
        switchSheriff(event.getSheriffSwitchNumber());
    }

    private void switchSheriff(Integer sheriffSwitchNumber) {
        if (sheriffSwitchNumber == null) {
            sheriffSwitchNumber = 0;
        }
        if (sheriffSwitchNumber != 0 && !roomState.getPlaySeatInfoBySeatNumber(sheriffSwitchNumber).isAlive()) {
            throw new RoomBusinessException("该玩家已死亡,无法移交警徽");
        }
        //重新设置警长
        this.roomState.getSheriffRecord().setSheriff(sheriffSwitchNumber);
        //设置缓存事件
        this.roomState.setStatus(roomState.getSheriffRecord().getAfterSwitchSheriff());
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        displayInfo.addAcceptableEventType(JudgeEventType.SHERIFF_SWITCH);
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        switch (event.getEventType()) {
            case SHERIFF_SWITCH:
                sheriffSwitch(event);
                break;
        }
        return roomState;
    }

    private void sheriffSwitch(PlayerEvent event) {
        switchSheriff(event.getSheriffSwitchNumber());
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        //放开票型展示
        displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        if (roomState.getSheriffRecord().getSheriff() == seatNumber) {
            displayInfo.addAcceptableEventType(PlayerEventType.SHERIFF_SWITCH);
        }
        return displayInfo;
    }
}
